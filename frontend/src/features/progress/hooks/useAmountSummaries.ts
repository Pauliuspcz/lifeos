import { useEffect, useState } from "react";
import type { Goal } from "../../goals";
import { fetchGoalSummary } from "../api";
import type { GoalProgressSummary, Progress } from "../types";

export function useAmountSummaries(goals: Goal[], entries: Progress[]) {
  const [summaries, setSummaries] = useState<
    Record<number, GoalProgressSummary>
  >({});

  useEffect(() => {
    let cancelled = false;
    const amountGoals = goals.filter((g) => g.trackingType === "AMOUNT");
    Promise.all(amountGoals.map((g) => fetchGoalSummary(g.id)))
      .then((list) => {
        if (cancelled) return;
        const byGoal: Record<number, GoalProgressSummary> = {};
        for (const s of list) byGoal[s.goalId] = s;
        setSummaries(byGoal);
      })
      .catch(() => {
        if (!cancelled) setSummaries({});
      });
    return () => {
      cancelled = true;
    };
  }, [goals, entries]);

  return summaries;
}
