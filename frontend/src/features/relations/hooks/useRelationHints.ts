import { useEffect, useState } from "react";
import { fetchRelationHints } from "../api";
import type { GoalRef, RelationHint, RelationRequest } from "../types";

export function useRelationHints(
  goals: GoalRef[],
  candidate: GoalRef | null,
  enabled = true,
): RelationHint[] {
  const [hints, setHints] = useState<RelationHint[]>([]);
  const key = JSON.stringify({ goals, candidate });

  useEffect(() => {
    if (!enabled) return;
    let cancelled = false;
    fetchRelationHints(JSON.parse(key) as RelationRequest)
      .then((result) => {
        if (!cancelled) setHints(result);
      })
      .catch(() => {
        if (!cancelled) setHints([]);
      });
    return () => {
      cancelled = true;
    };
  }, [key, enabled]);

  return enabled ? hints : [];
}
