import type { CSSProperties, ReactNode } from "react";
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

function suggestionText(
  goal: Goal,
  report: GoalReport | undefined,
  unit: string,
  streak: number,
): ReactNode {
  const suggestion = report?.suggestion;
  switch (suggestion?.recommendation) {
    case "INACTIVE":
      return streak > 0
        ? `Forgot to log? Log ${timesOf(goal)} this ${unit} to keep your streak.`
        : `Start fresh with ${timesOf(goal)} this ${unit}?`;
    case "RAISE":
      return (
        <>
          Target reached every {unit} lately.{" "}
          <span className="insight-try">
            Try {suggestion.suggestedTarget} per {unit}?
          </span>
        </>
      );
    case "LOWER":
      return (
        <>
          Tough few {unit}s.{" "}
          <span className="insight-try">
            Try {suggestion.suggestedTarget} per {unit}
          </span>{" "}
          to build the habit?
        </>
      );
    case "KEEP":
      return "Keep it up!";
    default:
      return null;
  }
}

function Stat({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="insight-stat">
      <dt>{label}</dt>
      <dd>{children}</dd>
    </div>
  );
}

export default function GoalInsightCard({ goal, report }: Props) {
  const period = periodOf(goal);
  const unit = PERIOD_LABEL[period];
  const isAmount = goal.trackingType === "AMOUNT";
  const last30 = report?.last30Days ?? 0;
  const diff = last30 - (report?.previous30Days ?? 0);
  const total = report?.totalSessions ?? 0;
  const streak = report?.currentStreak ?? 0;
  const streakUnit = streak === 1 ? unit : `${unit}s`;
  const successRate = report?.successRate ?? 0;
  const history = report?.history ?? [];
  const maxCount = Math.max(timesOf(goal), ...history.map((h) => h.count));
  const tip = isAmount ? null : suggestionText(goal, report, unit, streak);

  return (
    <div
      className="insight-goal"
      style={{ "--goal-color": goal.color ?? "var(--accent)" } as CSSProperties}
    >
      <div className="insight-goal-head">
        <h2>{goal.title}</h2>
        {!isAmount && (
          <span className="muted">
            {streak > 0 ? `${streak} ${streakUnit} in a row` : "No streak yet"}
          </span>
        )}
      </div>

      <dl className="insight-stats">
        <Stat label={isAmount ? "Days logged" : "Check-ins"}>{total}</Stat>
        <Stat label="Last 30 days">
          {last30} <span className="muted">({trendLabel(diff)})</span>
        </Stat>
        {!isAmount && <Stat label="Completion rate">{successRate}%</Stat>}
        <Stat label="Last check-in">
          {lastCheckinLabel(report?.lastDate ?? null)}
        </Stat>
      </dl>

      {!isAmount && (
        <>
          <p className="insight-label">Last 12 {unit}s</p>
          <div className="insight-bars">
            {history.map((h) => (
              <div
                className="insight-bar"
                key={h.periodStart}
                title={`${h.periodStart}: ${h.count}`}
              >
                <div
                  className="insight-bar-fill"
                  style={{ height: `${(h.count / maxCount) * 100}%` }}
                />
              </div>
            ))}
          </div>
          {tip && <p className="insight-tip">{tip}</p>}
        </>
      )}
    </div>
  );
}
