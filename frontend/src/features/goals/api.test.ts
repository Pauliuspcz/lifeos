import { beforeEach, describe, expect, it, vi } from "vitest";
import { request } from "../../shared/api/client";
import { archiveGoal, createGoal, listGoals } from "./api";

vi.mock("../../shared/api/client", () => ({ request: vi.fn() }));

const requestMock = vi.mocked(request);

beforeEach(() => {
  requestMock.mockReset();
});

describe("goals api", () => {
  it("lists all goals with authentication", async () => {
    const goals = [{ id: 1 }];
    requestMock.mockResolvedValue(goals);

    await expect(listGoals()).resolves.toBe(goals);

    expect(requestMock).toHaveBeenCalledWith(
      "GET",
      "/api/goal",
      undefined,
      true,
    );
  });

  it("filters the list by status", async () => {
    requestMock.mockResolvedValue([]);

    await listGoals("ACTIVE");

    expect(requestMock).toHaveBeenCalledWith(
      "GET",
      "/api/goal?status=ACTIVE",
      undefined,
      true,
    );
  });

  it("creates a goal with the request body", async () => {
    const body = {
      title: "Run",
      description: null,
      period: "WEEKLY" as const,
      timesPerPeriod: 3,
      amountPerSession: null,
    };
    requestMock.mockResolvedValue({ id: 5, ...body });

    await createGoal(body);

    expect(requestMock).toHaveBeenCalledWith("POST", "/api/goal", body, true);
  });

  it("archives a goal by id", async () => {
    requestMock.mockResolvedValue({ id: 7 });

    await archiveGoal(7);

    expect(requestMock).toHaveBeenCalledWith(
      "PATCH",
      "/api/goal/7/archive",
      undefined,
      true,
    );
  });
});
