export type WeatherCondition =
  | "CLEAR"
  | "CLOUDY"
  | "FOG"
  | "DRIZZLE"
  | "FREEZING_RAIN"
  | "RAIN"
  | "SNOW"
  | "THUNDERSTORM"
  | "UNKNOWN";

export type Rating = "GOOD" | "OK" | "POOR";

export type Weather = {
  latitude: number;
  longitude: number;
  observedAt: string;
  temperature: number;
  feelsLike: number;
  precipitationMm: number;
  windSpeedKmh: number;
  condition: WeatherCondition;
  day: boolean;
  stale: boolean;
};

export type WeatherRecommendation = {
  weather: Weather;
  rating: Rating;
  reason: string;
};

export const CONDITION_LABEL: Record<WeatherCondition, string> = {
  CLEAR: "Clear",
  CLOUDY: "Cloudy",
  FOG: "Fog",
  DRIZZLE: "Drizzle",
  FREEZING_RAIN: "Freezing rain",
  RAIN: "Rain",
  SNOW: "Snow",
  THUNDERSTORM: "Thunderstorm",
  UNKNOWN: "Unknown",
};
