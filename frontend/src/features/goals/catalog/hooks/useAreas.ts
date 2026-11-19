import { useEffect, useState } from "react";
import { listCategories } from "../api";
import { listGoals } from "../../api";
import type { Category } from "../types";
import { ApiError } from "../../../../shared/api/client";

export function useAreas() {
  const [areas, setAreas] = useState<Category[]>([]);
  const [counts, setCounts] = useState<Record<number, number>>({});
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    let cancelled = false;
    listCategories()
      .then((list) => {
        if (!cancelled) setAreas(list);
      })
      .catch((err) => {
        if (cancelled) return;
        const code = err instanceof ApiError ? ` (${err.status})` : "";
        setLoadError("Could not load areas" + code);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    listGoals("ACTIVE")
      .then((goals) => {
        if (cancelled) return;
        const c: Record<number, number> = {};
        for (const g of goals) c[g.categoryId] = (c[g.categoryId] ?? 0) + 1;
        setCounts(c);
      })
      .catch(() => {});

    return () => {
      cancelled = true;
    };
  }, []);

  return { areas, counts, loading, loadError };
}
