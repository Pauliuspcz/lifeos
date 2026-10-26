import { beforeEach, describe, expect, it, vi } from "vitest";
import { request } from "../../shared/api/client";
import { createProgress, deleteProgress, listProgress } from "./api";

vi.mock("../../shared/api/client", () => ({ request: vi.fn() }));

const requestMock = vi.mocked(request);

beforeEach(() => {
  requestMock.mockReset();
});

describe("progress api", () => {
  it("lists progress with authentication", async () => {
    const entries = [{ id: 1 }];
    requestMock.mockResolvedValue(entries);

    await expect(listProgress()).resolves.toBe(entries);

    expect(requestMock).toHaveBeenCalledWith(
      "GET",
      "/api/progress",
      undefined,
      true,
    );
  });

  it("creates progress with the request body", async () => {
    const body = { goalId: 2, date: "2026-10-28", amount: null };
    requestMock.mockResolvedValue({ id: 5, ...body });

    await createProgress(body);

    expect(requestMock).toHaveBeenCalledWith(
      "POST",
      "/api/progress",
      body,
      true,
    );
  });

  it("deletes progress by id", async () => {
    requestMock.mockResolvedValue(undefined);

    await deleteProgress(5);

    expect(requestMock).toHaveBeenCalledWith(
      "DELETE",
      "/api/progress/5",
      undefined,
      true,
    );
  });
});
