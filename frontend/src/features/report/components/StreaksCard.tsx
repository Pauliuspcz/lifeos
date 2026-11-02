import { type Goal, PERIOD_LABEL, periodOf } from "../../goals";
import type { GoalReport } from "../types";

type Props = {
  goals: Goal[];
  reports: GoalReport[];
};

export default function StreaksCard({ goals, reports }: Props) {
  const active = goals.filter(
    (g) => (reports.find((r) => r.goalId === g.id)?.currentStreak ?? 0) > 0,
  );

  return (
    <div className="streaks-box">
      <div className="box-header">
        <h2>Streaks</h2>
      </div>
      {active.length === 0 && <p className="muted">No active streaks yet.</p>}
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
