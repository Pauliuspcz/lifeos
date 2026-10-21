import { act, cleanup, renderHook, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ApiError } from "../../../shared/api/client";
import { archiveGoal, createGoal, listGoals } from "../api";
import type { Goal } from "../types";
import { useGoals } from "./useGoals";

vi.mock("../api", () => ({
  listGoals: vi.fn(),
  createGoal: vi.fn(),
  archiveGoal: vi.fn(),
}));

const listMock = vi.mocked(listGoals);
const createMock = vi.mocked(createGoal);
const archiveMock = vi.mocked(archiveGoal);

const goal = (id: number, status: Goal["status"] = "ACTIVE"): Goal => ({
  id,
  title: `Goal ${id}`,
  description: null,
  period: "WEEKLY",
  timesPerPeriod: 1,
  amountPerSession: null,
  unit: null,
  status,
  templateId: null,
  version: 0,
  createdAt: "",
  updatedAt: "",
  archivedAt: null,
});

const newGoal = {
  title: "New",
  description: null,
  period: "WEEKLY" as const,
  timesPerPeriod: 1,
  amountPerSession: null,
};

beforeEach(() => {
  listMock.mockReset();
  createMock.mockReset();
  archiveMock.mockReset();
});

afterEach(() => {
  cleanup();
});

async function loaded(status?: Goal["status"]) {
  const hook = renderHook(() => useGoals(status));
  await waitFor(() => expect(hook.result.current.loading).toBe(false));
  return hook;
}

describe("useGoals", () => {
  it("loads goals for the given status", async () => {
    listMock.mockResolvedValue([goal(1)]);

    const { result } = await loaded("ACTIVE");

    expect(listMock).toHaveBeenCalledWith("ACTIVE");
    expect(result.current.goals.map((g) => g.id)).toEqual([1]);
  });

  it("puts a created goal first", async () => {
    listMock.mockResolvedValue([goal(1)]);
    createMock.mockResolvedValue(goal(2));
    const { result } = await loaded("ACTIVE");

    await act(async () => {
      await result.current.addGoal(newGoal);
    });

    expect(result.current.goals.map((g) => g.id)).toEqual([2, 1]);
  });

  it("removes an archived goal from the active list", async () => {
    listMock.mockResolvedValue([goal(1)]);
    archiveMock.mockResolvedValue(goal(1, "ARCHIVED"));
    const { result } = await loaded("ACTIVE");

    await act(async () => {
      await result.current.archiveGoal(1);
    });

    expect(result.current.goals).toEqual([]);
  });

  it("reports a load error with the status code", async () => {
    listMock.mockRejectedValue(new ApiError(500));

    const { result } = await loaded("ACTIVE");

    expect(result.current.loadError).toBe("Kunne ikke laste mål (500)");
  });
});
