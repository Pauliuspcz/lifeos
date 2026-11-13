import { useWeather } from "../hooks/useWeather";
import { CONDITION_LABEL } from "../types";

export default function WeatherChip() {
  const { recommendation } = useWeather();
  if (!recommendation) return null;
  const { temperature, condition } = recommendation.weather;

  return (
    <span className="topbar-weather" title={recommendation.reason}>
      {Math.round(temperature)}°C · {CONDITION_LABEL[condition]}
    </span>
  );
}
