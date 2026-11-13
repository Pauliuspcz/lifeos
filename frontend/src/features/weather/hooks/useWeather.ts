import { useEffect, useState } from "react";
import { getWeather } from "../api";
import type { WeatherRecommendation } from "../types";

export function useWeather() {
  const [recommendation, setRecommendation] =
    useState<WeatherRecommendation | null>(null);

  useEffect(() => {
    if (!navigator.geolocation) return;
    let cancelled = false;
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        getWeather(pos.coords.latitude, pos.coords.longitude)
          .then((r) => {
            if (!cancelled) setRecommendation(r);
          })
          .catch(() => {});
      },
      () => {},
      { maximumAge: 10 * 60 * 1000 },
    );
    return () => {
      cancelled = true;
    };
  }, []);

  return { recommendation };
}
