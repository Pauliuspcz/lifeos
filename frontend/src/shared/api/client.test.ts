import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ApiError, request } from "./client";
import { tokenStore } from "./tokens";

type Call = { url: string; headers: Record<string, string> };

const fetchMock = vi.fn();
let calls: Call[];
let queue: Response[];

const tokens = (n: number) => ({
  accessToken: `access-${n}`,
  refreshToken: `refresh-${n}`,
  tokenType: "Bearer",
  expiresInSeconds: 3600,
});

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

function respondWith(...responses: Response[]) {
  queue = [...responses];
}

function pathOf(url: string): string {
  return new URL(url, "http://localhost").pathname;
}

beforeEach(() => {
  localStorage.clear();
  calls = [];
  queue = [];
  fetchMock.mockReset();
  fetchMock.mockImplementation(async (url: string, init?: RequestInit) => {
    calls.push({
      url,
      headers: (init?.headers ?? {}) as Record<string, string>,
    });
    const next = queue.shift();
    if (!next) throw new Error(`Unexpected fetch call to ${url}`);
    return next;
  });
  vi.stubGlobal("fetch", fetchMock);
});

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("request", () => {
  it("sends the access token when auth is true", async () => {
    tokenStore.save(tokens(1));
    respondWith(json({ ok: true }));

    const result = await request("GET", "/profile/me", undefined, true);

    expect(result).toEqual({ ok: true });
    expect(calls[0].headers.Authorization).toBe("Bearer access-1");
  });

  it("sends no Authorization header when auth is false", async () => {
    tokenStore.save(tokens(1));
    respondWith(json({ ok: true }));

    await request("POST", "/auth/login", { email: "a@b.no" });

    expect(calls[0].headers.Authorization).toBeUndefined();
  });

  it("returns undefined for 204", async () => {
    respondWith(new Response(null, { status: 204 }));

    await expect(request("POST", "/auth/logout", {})).resolves.toBeUndefined();
  });

  it("refreshes once and retries the request on 401", async () => {
    tokenStore.save(tokens(1));
    respondWith(json({}, 401), json(tokens(2)), json({ ok: true }));

    const result = await request("GET", "/profile/me", undefined, true);

    expect(result).toEqual({ ok: true });
    expect(calls).toHaveLength(3);
    expect(pathOf(calls[1].url)).toBe("/auth/refresh");
    expect(calls[2].headers.Authorization).toBe("Bearer access-2");
    expect(tokenStore.getRefresh()).toBe("refresh-2");
  });

  it("shares one refresh between parallel 401 responses", async () => {
    tokenStore.save(tokens(1));
    let refreshCalls = 0;
    fetchMock.mockImplementation(async (url: string, init: RequestInit) => {
      if (url.endsWith("/auth/refresh")) {
        refreshCalls += 1;
        return json(tokens(2));
      }
      const auth = (init.headers as Record<string, string>).Authorization;
      return auth === "Bearer access-2" ? json({ ok: true }) : json({}, 401);
    });

    await Promise.all([
      request("GET", "/api/goal", undefined, true),
      request("GET", "/profile/me", undefined, true),
    ]);

    expect(refreshCalls).toBe(1);
  });

  it("clears the tokens and throws 401 when the refresh is rejected", async () => {
    tokenStore.save(tokens(1));
    respondWith(json({}, 401), json({}, 401));

    await expect(
      request("GET", "/profile/me", undefined, true),
    ).rejects.toMatchObject({ status: 401 });
    expect(tokenStore.getAccess()).toBeNull();
    expect(tokenStore.getRefresh()).toBeNull();
  });

  it("keeps the tokens when the refresh fails with a server error", async () => {
    tokenStore.save(tokens(1));
    respondWith(json({}, 401), json({}, 500));

    await expect(
      request("GET", "/profile/me", undefined, true),
    ).rejects.toMatchObject({ status: 401 });
    expect(tokenStore.getAccess()).toBe("access-1");
    expect(tokenStore.getRefresh()).toBe("refresh-1");
  });

  it("keeps the tokens when the refresh request fails on the network", async () => {
    tokenStore.save(tokens(1));
    fetchMock.mockImplementation(async (url: string) => {
      if (url.endsWith("/auth/refresh")) throw new TypeError("Failed to fetch");
      return json({}, 401);
    });

    await expect(
      request("GET", "/profile/me", undefined, true),
    ).rejects.toMatchObject({ status: 401 });
    expect(tokenStore.getRefresh()).toBe("refresh-1");
  });

  it("does not call refresh when there is no refresh token", async () => {
    respondWith(json({}, 401));

    await expect(
      request("GET", "/profile/me", undefined, true),
    ).rejects.toMatchObject({ status: 401 });
    expect(calls).toHaveLength(1);
  });
});

describe("ApiError", () => {
  it("carries message and fieldErrors from the backend", async () => {
    respondWith(
      json(
        {
          status: 400,
          error: "Bad Request",
          message: "Validation failed",
          fieldErrors: { title: "must not be blank" },
        },
        400,
      ),
    );

    const error = await request("POST", "/api/goal", {}).catch(
      (e: unknown) => e,
    );

    expect(error).toBeInstanceOf(ApiError);
    expect(error).toMatchObject({
      status: 400,
      message: "Validation failed",
      fieldErrors: { title: "must not be blank" },
    });
  });

  it("falls back to the status when the body is empty", async () => {
    respondWith(new Response(null, { status: 401 }));

    const error = await request("GET", "/profile/me").catch((e: unknown) => e);

    expect(error).toMatchObject({
      status: 401,
      message: "HTTP 401",
      fieldErrors: {},
    });
  });

  it("falls back to the status when the body is not JSON", async () => {
    respondWith(new Response("Bad gateway", { status: 502 }));

    const error = await request("GET", "/profile/me").catch((e: unknown) => e);

    expect(error).toMatchObject({ status: 502, message: "HTTP 502" });
  });
});
