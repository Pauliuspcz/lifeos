import { useEffect, useState } from "react";
import { CreateGoalRequest, Types } from "../features/goals/types";

export function useGoals() {
  const [goals, setGoals] = useState<Types[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    fetch("/api/goal/all")
      .then((res) => {
        if (!res.ok)
          throw new Error("Kunne ikke laste mål (" + res.status + ")");
        return res.json();
      })
      .then(setGoals)
      .catch((err) => setLoadError((err as Error).message))
      .finally(() => setLoading(false));
  }, []);

  async function addGoal(req: CreateGoalRequest) {
    const res = await fetch("/api/goal", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(req),
    });
    if (!res.ok)
      throw new Error("Kunne ikke opprette mål (" + res.status + ")");
    const created: Types = await res.json();
    setGoals((prev) => [...prev, created]);
  }

  async function archiveGoal(goalId: number) {
    const res = await fetch(`/api/goal/${goalId}/archive`, { method: "PATCH" });
    if (!res.ok)
      throw new Error("Kunne ikke arkivere mål (" + res.status + ")");
    const updated: Types = await res.json();
    setGoals((prev) =>
      prev.map((g) => (g.goalId === updated.goalId ? updated : g)),
    );
  }
  return { goals, loading, loadError, addGoal, archiveGoal };
}
