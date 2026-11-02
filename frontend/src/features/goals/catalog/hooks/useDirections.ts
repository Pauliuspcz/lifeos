import { useEffect, useState } from "react";
import { listDirections } from "../api";
import type { Direction } from "../types";
import { ApiError } from "../../../../shared/api/client";

export function useDirections() {
  const [directions, setDirections] = useState<Direction[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    let cancelled = false;
    listDirections()
      .then((list) => {
        if (!cancelled) setDirections(list);
      })
      .catch((err) => {
        if (cancelled) return;
        const code = err instanceof ApiError ? ` (${err.status})` : "";
        setLoadError("Could not load directions" + code);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return { directions, loading, loadError };
}
