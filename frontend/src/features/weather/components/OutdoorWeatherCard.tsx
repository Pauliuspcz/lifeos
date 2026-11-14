import type { Goal } from "../../goals";
import { getMyWeather } from "../api";
import { useWeather } from "../hooks/useWeather";
import { CONDITION_LABEL } from "../types";

type Props = {
  goals: Goal[];
};

export default function OutdoorWeatherCard({ goals }: Props) {
  const { recommendation } = useWeather(getMyWeather);
  const names = [
    ...new Set(goals.filter((g) => g.outdoor).map((g) => g.title)),
  ];
  if (!recommendation || names.length === 0) return null;
  const { rating, reason, weather } = recommendation;
  const temp = Math.round(weather.temperature).toString().replace("-", "−");

  return (
    <div className="outdoor-box">
      <p className="outdoor-label">Outdoor</p>
      <p className="outdoor-temp">
        {temp}°<span>{CONDITION_LABEL[weather.condition]}</span>
      </p>
      <p
        className={rating === "POOR" ? "outdoor-reason poor" : "outdoor-reason"}
      >
        {reason}
      </p>
      <p className="outdoor-goals">{names.join(" · ")}</p>
    </div>
  );
}
