import { useEffect, useState } from "react";
import { ApiError } from "../../../shared/api/client";
import { createProgress, deleteProgress, listProgress } from "../api";
import type { CreateProgressRequest, Progress } from "../types";

export function useProgress() {
  const [entries, setEntries] = useState<Progress[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    let cancelled = false;
    listProgress()
      .then((list) => {
        if (!cancelled) setEntries(list);
      })
      .catch((err) => {
        if (cancelled) return;
        const code = err instanceof ApiError ? ` (${err.status})` : "";
        setLoadError("Could not load progress" + code);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function addProgress(req: CreateProgressRequest) {
    const created = await createProgress(req);
    setEntries((prev) => [created, ...prev]);
  }

  async function removeProgress(id: number) {
    await deleteProgress(id);
    setEntries((prev) => prev.filter((p) => p.id !== id));
  }

  return { entries, loading, loadError, addProgress, removeProgress };
}
