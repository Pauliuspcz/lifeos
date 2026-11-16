import { useEffect, useState } from "react";
import { listDirectionTemplates } from "../api";
import type { Template } from "../types";
import { ApiError } from "../../../../shared/api/client";

export function useDirectionTemplates(directionId: number) {
  const [templates, setTemplates] = useState<Template[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setLoadError("");
    listDirectionTemplates(directionId)
      .then((t) => {
        if (!cancelled) setTemplates(t);
      })
      .catch((err) => {
        if (cancelled) return;
        const code = err instanceof ApiError ? ` (${err.status})` : "";
        setLoadError("Could not load goals" + code);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [directionId]);

  return { templates, loading, loadError };
}
