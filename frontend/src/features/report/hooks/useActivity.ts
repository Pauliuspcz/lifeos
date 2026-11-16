import { useEffect, useState } from "react";
import { ApiError } from "../../../shared/api/client";
import { getActivity } from "../api";
import type { ActivityReport } from "../types";

export function useActivity() {
  const [activity, setActivity] = useState<ActivityReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    let cancelled = false;
    getActivity()
      .then((a) => {
        if (!cancelled) setActivity(a);
      })
      .catch((err) => {
        if (cancelled) return;
        const code = err instanceof ApiError ? ` (${err.status})` : "";
        setLoadError("Could not load activity" + code);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return { activity, loading, loadError };
}
