import { useCallback, useEffect, useState } from "react";
import { ApiError } from "../../../shared/api/client";
import { createProgress, deleteProgress, fetchProgressWindow } from "../api";
import type { ProgressWindow } from "../types";

export function useProgressWindow() {
  const [data, setData] = useState<ProgressWindow | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  const load = useCallback(async () => {
    try {
      setData(await fetchProgressWindow());
      setLoadError("");
    } catch (err) {
      const code = err instanceof ApiError ? ` (${err.status})` : "";
      setLoadError("Could not load logs" + code);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  async function logDay(goalId: number, date: string) {
    await createProgress({ goalId, date, amount: null });
    await load();
  }

  async function unlogDay(id: number) {
    await deleteProgress(id);
    await load();
  }

  return { data, loading, loadError, logDay, unlogDay };
}
