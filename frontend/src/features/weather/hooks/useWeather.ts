import { useEffect, useState } from "react";
import type { WeatherRecommendation } from "../types";

type FetchWeather = (
  lat: number,
  lon: number,
) => Promise<WeatherRecommendation | undefined>;

export function useWeather(fetchWeather: FetchWeather) {
  const [recommendation, setRecommendation] =
    useState<WeatherRecommendation | null>(null);

  useEffect(() => {
    if (!navigator.geolocation) return;
    let cancelled = false;
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        fetchWeather(pos.coords.latitude, pos.coords.longitude)
          .then((r) => {
            if (!cancelled) setRecommendation(r ?? null);
          })
          .catch(() => {});
      },
      () => {},
      { maximumAge: 10 * 60 * 1000 },
    );
    return () => {
      cancelled = true;
    };
  }, [fetchWeather]);

  return { recommendation };
}
