import { useEffect, useState } from "react";
import { ApiError } from "../../../shared/api/client";
import type { Goal } from "../../goals";
import { getGoalReport } from "../api";
import type { GoalReport } from "../types";

export function useGoalReports(goals: Goal[], refreshKey?: unknown) {
  const [reports, setReports] = useState<GoalReport[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    Promise.all(goals.map((g) => getGoalReport(g.id)))
      .then((list) => {
        if (!cancelled) setReports(list);
      })
      .catch((err) => {
        if (cancelled) return;
        const code = err instanceof ApiError ? ` (${err.status})` : "";
        setLoadError("Could not load insights" + code);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [goals, refreshKey]);

  return { reports, loading, loadError };
}
