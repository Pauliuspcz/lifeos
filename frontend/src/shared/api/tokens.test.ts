import { beforeEach, describe, expect, it, vi } from "vitest";
import { tokenStore } from "./tokens";

const auth = {
  accessToken: "a",
  refreshToken: "r",
  tokenType: "Bearer",
  expiresInSeconds: 3600,
};

beforeEach(() => {
  localStorage.clear();
});

describe("tokenStore", () => {
  it("has a session only while a refresh token is stored", () => {
    expect(tokenStore.hasSession()).toBe(false);
    tokenStore.save(auth);
    expect(tokenStore.hasSession()).toBe(true);
    tokenStore.clear();
    expect(tokenStore.hasSession()).toBe(false);
  });

  it("notifies subscribers on save and clear", () => {
    const listener = vi.fn();
    const unsubscribe = tokenStore.subscribe(listener);

    tokenStore.save(auth);
    tokenStore.clear();

    expect(listener).toHaveBeenCalledTimes(2);
    unsubscribe();
  });

  it("stops notifying after unsubscribe", () => {
    const listener = vi.fn();
    tokenStore.subscribe(listener)();

    tokenStore.save(auth);

    expect(listener).not.toHaveBeenCalled();
  });

  it("notifies when another tab changes the stored tokens", () => {
    const listener = vi.fn();
    const unsubscribe = tokenStore.subscribe(listener);

    window.dispatchEvent(new StorageEvent("storage", { key: "refreshToken" }));

    expect(listener).toHaveBeenCalledTimes(1);
    unsubscribe();
  });
});
