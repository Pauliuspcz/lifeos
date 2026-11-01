import { useEffect, useState } from "react";
import { ApiError } from "../../../shared/api/client";
import {
  archiveGoal as archiveGoalRequest,
  createGoal,
  deleteGoal as deleteGoalRequest,
  listGoals,
  unarchiveGoal as unarchiveGoalRequest,
} from "../api";
import type { CreateGoalRequest, Goal, GoalStatus } from "../types";

export function useGoals(status?: GoalStatus) {
  const [goals, setGoals] = useState<Goal[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setLoadError("");
    listGoals(status)
      .then((list) => {
        if (!cancelled) setGoals(list);
      })
      .catch((err) => {
        if (cancelled) return;
        const code = err instanceof ApiError ? ` (${err.status})` : "";
        setLoadError("Could not load goals" + code);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [status]);

  async function addGoal(req: CreateGoalRequest) {
    const created = await createGoal(req);
    if (!status || created.status === status) {
      setGoals((prev) => [created, ...prev]);
    }
  }

  function applyUpdated(updated: Goal) {
    const stillMatches = !status || updated.status === status;
    setGoals((prev) =>
      stillMatches
        ? prev.map((g) => (g.id === updated.id ? updated : g))
        : prev.filter((g) => g.id !== updated.id),
    );
  }

  async function archiveGoal(id: number) {
    applyUpdated(await archiveGoalRequest(id));
  }

  async function unarchiveGoal(id: number) {
    applyUpdated(await unarchiveGoalRequest(id));
  }

  async function removeGoal(id: number) {
    await deleteGoalRequest(id);
    setGoals((prev) => prev.filter((g) => g.id !== id));
  }

  return {
    goals,
    loading,
    loadError,
    addGoal,
    archiveGoal,
    unarchiveGoal,
    removeGoal,
  };
}
