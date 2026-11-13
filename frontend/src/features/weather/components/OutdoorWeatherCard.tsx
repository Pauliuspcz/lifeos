import type { Goal } from "../../goals";
import { getMyWeather } from "../api";
import { useWeather } from "../hooks/useWeather";
import { RATING_TITLE } from "../types";

type Props = {
  goals: Goal[];
};

export default function OutdoorWeatherCard({ goals }: Props) {
  const { recommendation } = useWeather(getMyWeather);
  const outdoor = goals.filter((g) => g.outdoor);
  if (!recommendation || outdoor.length === 0) return null;

  return (
    <div className="outdoor-box">
      <div className="box-header">
        <h2>{RATING_TITLE[recommendation.rating]}</h2>
      </div>
      <p className="muted">{recommendation.reason}</p>
      {outdoor.map((g) => (
        <div className="goal-row" key={g.id}>
          <div className="goal-top">
            <span>{g.title}</span>
          </div>
        </div>
      ))}
    </div>
  );
}
