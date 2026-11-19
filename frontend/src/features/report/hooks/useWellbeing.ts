import { useEffect, useState } from "react";
import { getWellbeing } from "../api";
import type { Wellbeing } from "../types";

export function useWellbeing() {
  const [wellbeing, setWellbeing] = useState<Wellbeing | null>(null);

  useEffect(() => {
    let cancelled = false;
    getWellbeing()
      .then((w) => {
        if (!cancelled) setWellbeing(w);
      })
      .catch(() => {
        if (!cancelled) setWellbeing(null);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return { wellbeing };
}
