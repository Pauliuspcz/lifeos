import { request } from "../../shared/api/client";
import { weatherEndpoints } from "./endpoints";
import type { WeatherRecommendation } from "./types";

export const getWeather = (lat: number, lon: number) =>
  request<WeatherRecommendation>(
    "GET",
    weatherEndpoints.current(lat, lon),
    undefined,
    true,
  );

export const getMyWeather = (lat: number, lon: number) =>
  request<WeatherRecommendation | undefined>(
    "GET",
    weatherEndpoints.mine(lat, lon),
    undefined,
    true,
  );
