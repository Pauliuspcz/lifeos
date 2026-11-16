import type { Goal } from "../../goals";
import { getMyWeather, getWeather } from "../api";
import { useWeather } from "../hooks/useWeather";
import { CONDITION_LABEL } from "../types";

type Props = {
  goals: Goal[];
};

export default function OutdoorWeatherCard({ goals }: Props) {
  const { recommendation: current } = useWeather(getWeather);
  const { recommendation: mine } = useWeather(getMyWeather);
  const names = [
    ...new Set(goals.filter((g) => g.outdoor).map((g) => g.title)),
  ];
  const temp = current
    ? Math.round(current.weather.temperature).toString().replace("-", "−")
    : "—";

  return (
    <div className="outdoor-box">
      <p className="outdoor-label">Weather</p>
      <p className="outdoor-temp">
        {temp}°
        {current && <span>{CONDITION_LABEL[current.weather.condition]}</span>}
      </p>
      {mine && (
        <p
          className={
            mine.rating === "POOR" ? "outdoor-reason poor" : "outdoor-reason"
          }
        >
          {mine.reason}
        </p>
      )}
      {mine?.advice && <p className="outdoor-reason">{mine.advice}</p>}
      {names.length > 0 && <p className="outdoor-goals">{names.join(" · ")}</p>}
    </div>
  );
}
