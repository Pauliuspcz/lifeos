import { useEffect, useState } from "react";
import { fetchGoalSummary } from "../api";
import type { GoalProgressSummary } from "../types";

export function useGoalSummary(goalId: number, refreshKey: unknown) {
  const [summary, setSummary] = useState<GoalProgressSummary | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchGoalSummary(goalId)
      .then((s) => {
        if (!cancelled) setSummary(s);
      })
      .catch(() => {
        if (!cancelled) setSummary(null);
      });
    return () => {
      cancelled = true;
    };
  }, [goalId, refreshKey]);

  return summary;
}
