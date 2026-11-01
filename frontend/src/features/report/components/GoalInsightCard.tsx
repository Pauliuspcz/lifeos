import { Goal, PERIOD_LABEL, periodOf, timesOf } from "../../goals";
import { GoalReport } from "../types";
import { lastCheckinLabel } from "../utils/dates";

type Props = {
  goal: Goal;
  report?: GoalReport;
};

function trendLabel(diff: number): string {
  if (diff > 0) return `+${diff}`;
  if (diff < 0) return `-${Math.abs(diff)}`;
  return "±0";
}

export default function GoalInsightCard({ goal, report }: Props) {
  const period = periodOf(goal);
  const unit = PERIOD_LABEL[period];
  const last30 = report?.last30Days ?? 0;
  const diff = last30 - (report?.previous30Days ?? 0);
  const total = report?.totalSessions ?? 0;
  const streak = report?.currentStreak ?? 0;
  const streakUnit = streak === 1 ? unit : `${unit}s`;
  const successRate = report?.successRate ?? 0;
  const history = report?.history ?? [];
  const maxCount = Math.max(timesOf(goal), ...history.map((h) => h.count));

  return (
    <div className="insight-goal">
      <div className="box-header">
        <h2>{goal.title}</h2>
        <span className="muted">
          {streak > 0 ? `${streak} ${streakUnit} in a row` : "No streak yet"}
        </span>
      </div>
      <p>Check-ins: {total}</p>
      <p>
        Last 30 days: {last30} ({trendLabel(diff)})
      </p>
      <p>Completion rate: {successRate}%</p>
      <p className="muted">Last 12 {unit}s:</p>
      <div className="history">
        {history.map((h) => (
          <div
            className="week-bar"
            key={h.periodStart}
            title={`${h.periodStart}: ${h.count}`}
          >
            <div
              className={"week-fill" + (h.reached ? "" : " missed")}
              style={{ height: `${(h.count / maxCount) * 100}%` }}
            />
          </div>
        ))}
      </div>
      <p>
        Last check-in:
        {lastCheckinLabel(report?.lastDate ?? null)}
      </p>
    </div>
  );
}
