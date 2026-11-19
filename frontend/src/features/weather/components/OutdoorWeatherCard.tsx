import type { Goal } from "../../goals";
import { getMyWeather, getWeather } from "../api";
import { useWeather } from "../hooks/useWeather";
import { CONDITION_LABEL } from "../types";
import { format, parseISO } from "date-fns";

type Props = {
  goals: Goal[];
};

const formatTemp = (n: number) => Math.round(n).toString().replace("-", "−");

export default function OutdoorWeatherCard({ goals }: Props) {
  const { recommendation: current } = useWeather(getWeather);
  const { recommendation: mine } = useWeather(getMyWeather);
  const names = [
    ...new Set(goals.filter((g) => g.outdoor).map((g) => g.title)),
  ];
  const temp = current ? formatTemp(current.weather.temperature) : "—";
  const feels = current ? formatTemp(current.weather.feelsLike) : null;
  const reasonSaysFeels = mine?.reason.startsWith("Feels like") ?? false;
  const showFeels = feels !== null && feels !== temp && !reasonSaysFeels;
  const best = mine?.bestTime;
  const bestText = best
    ? `Better between ${format(parseISO(best.from), "HH:mm")} and ${format(parseISO(best.to), "HH:mm")}`
    : null;

  return (
    <div className="outdoor-box">
      <p className="outdoor-label">Weather</p>
      <p className="outdoor-temp">
        {temp}°
        {current && (
          <span>
            {CONDITION_LABEL[current.weather.condition]}
            {showFeels && ` · feels like ${feels}°`}
          </span>
        )}
      </p>
      {mine && (
        <p
          className={
            mine.rating === "POOR" ? "outdoor-reason poor" : "outdoor-reason"
          }
        >
          {mine.reason}
          {mine.rating !== "POOR" && mine.advice && ` · ${mine.advice}`}
        </p>
      )}
      {mine?.rating === "POOR" && mine.advice && (
        <p className="outdoor-reason">{mine.advice}</p>
      )}
      {bestText && <p className="outdoor-reason">{bestText}</p>}
      {names.length > 0 && (
        <div className="outdoor-goals">
          <p className="outdoor-label">Outdoor goals</p>
          <p className="outdoor-goal">{names.join(" · ")}</p>
        </div>
      )}
    </div>
  );
}
