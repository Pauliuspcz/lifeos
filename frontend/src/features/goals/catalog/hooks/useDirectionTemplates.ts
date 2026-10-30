import { useEffect, useState } from "react";

import { listCategories, listDirectionTemplates } from "../api";
import type { Category, Template } from "../types";
import { ApiError } from "../../../../shared/api/client";

export function useDirectionTemplates(directionId: number) {
  const [templates, setTemplates] = useState<Template[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setLoadError("");
    Promise.all([listDirectionTemplates(directionId), listCategories()])
      .then(([t, c]) => {
        if (cancelled) return;
        setTemplates(t);
        setCategories(c);
      })
      .catch((err) => {
        if (cancelled) return;
        const code = err instanceof ApiError ? ` (${err.status})` : "";
        setLoadError("Kunne ikke laste forslag" + code);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [directionId]);

  return { templates, categories, loading, loadError };
}
