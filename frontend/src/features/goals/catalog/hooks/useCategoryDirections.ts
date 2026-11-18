import { useEffect, useState } from "react";
import { listCategoryDirections } from "../api";
import type { Direction } from "../types";
import { ApiError } from "../../../../shared/api/client";

export function useCategoryDirections(categoryId: number) {
  const [directions, setDirections] = useState<Direction[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setLoadError("");
    listCategoryDirections(categoryId)
      .then((list) => {
        if (!cancelled) setDirections(list);
      })
      .catch((err) => {
        if (cancelled) return;
        const code = err instanceof ApiError ? ` (${err.status})` : "";
        setLoadError("Could not load focus areas" + code);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [categoryId]);

  return { directions, loading, loadError };
}
