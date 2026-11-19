import { type Goal, PERIOD_LABEL, periodOf } from "../../goals";
import type { GoalReport } from "../types";

type Props = {
  goals: Goal[];
  reports: GoalReport[];
  count: number | null;
  loadError: string;
};

export default function StreaksCard({
  goals,
  reports,
  count,
  loadError,
}: Props) {
  const active = goals.filter(
    (g) =>
      g.trackingType === "CHECK" &&
      (reports.find((r) => r.goalId === g.id)?.currentStreak ?? 0) > 0,
  );

  return (
    <div className="streaks-box">
      <div className="box-header">
        <h2>Streaks · {loadError ? (count ?? "…") : active.length}</h2>
        {loadError && <span className="muted">incl. archived</span>}
      </div>
      {loadError && <p className="muted">Could not load active goals</p>}
      {!loadError && active.length === 0 && (
        <p className="muted">No active streaks yet.</p>
      )}
      {active.map((g) => {
        const streak =
          reports.find((r) => r.goalId === g.id)?.currentStreak ?? 0;
        const unit = PERIOD_LABEL[periodOf(g)];
        return (
          <div className="goal-row" key={g.id}>
            <div className="goal-top">
              <span>{g.title}</span>
              <span className="muted">
                {streak} {unit}
                {streak === 1 ? "" : "s"}
              </span>
            </div>
          </div>
        );
      })}
    </div>
  );
}
