import { getWeather } from "../api";
import { CONDITION_LABEL } from "../types";
import { useWeather } from "../hooks/useWeather";

export default function WeatherChip() {
  const { recommendation } = useWeather(getWeather);
  if (!recommendation) return null;
  const { temperature, condition } = recommendation.weather;

  return (
    <span className="topbar-weather" title={recommendation.reason}>
      {Math.round(temperature)}°C · {CONDITION_LABEL[condition]}
    </span>
  );
}
