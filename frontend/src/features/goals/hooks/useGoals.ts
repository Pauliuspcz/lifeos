import { useEffect, useState } from "react";
import { ApiError } from "../../../shared/api/client";
import {
  archiveGoal as archiveGoalRequest,
  createGoal,
  listGoals,
} from "../api";
import type { CreateGoalRequest, Goal, GoalStatus } from "../types";

export function useGoals(status?: GoalStatus) {
  const [goals, setGoals] = useState<Goal[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    let cancelled = false;
    listGoals(status)
      .then((list) => {
        if (!cancelled) setGoals(list);
      })
      .catch((err) => {
        if (cancelled) return;
        const code = err instanceof ApiError ? ` (${err.status})` : "";
        setLoadError("Kunne ikke laste mål" + code);
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

  async function archiveGoal(id: number) {
    const updated = await archiveGoalRequest(id);
    const stillMatches = !status || updated.status === status;
    setGoals((prev) =>
      stillMatches
        ? prev.map((g) => (g.id === updated.id ? updated : g))
        : prev.filter((g) => g.id !== updated.id),
    );
  }

  return { goals, loading, loadError, addGoal, archiveGoal };
}
