import { Goal, PERIOD_LABEL, periodOf } from "../../goals";
import { GoalReport } from "../types";
import { averagePerPeriod, lastCheckinLabel } from "../utils/dates";

type Props = {
  goal: Goal;
  report?: GoalReport;
};

function formatAverage(value: number): string {
  return value.toLocaleString("en-GB", {
    maximumFractionDigits: 1,
  });
}

function trendLabel(diff: number): string {
  if (diff > 0) return `Up ${diff} from previous 30`;
  if (diff < 0) return `Down ${Math.abs(diff)} from previous 30`;
  return "same as previous 30";
}

export default function GoalInsightCard({ goal, report }: Props) {
  const period = periodOf(goal);
  const unit = PERIOD_LABEL[period];
  const last30 = report?.last30Days ?? 0;
  const diff = last30 - (report?.previous30Days ?? 0);
  const total = report?.totalSessions ?? 0;
  const average = formatAverage(averagePerPeriod(total, goal));
  const streak = report?.currentStreak ?? 0;
  const streakUnit = streak === 1 ? unit : `${unit}s`;

  return (
    <li>
      <h3>{goal.title}</h3>
      <p>Check-ins: {total}</p>
      <p>
        Last 30 days: {last30} ({trendLabel(diff)})
      </p>
      <p>{streak > 0 ? `${streak} ${streakUnit} in a row` : "No streak yet"}</p>
      <p>
        Last check-in:
        {lastCheckinLabel(report?.lastDate ?? null)}
      </p>
      <p>
        Average: {average} per {unit} (goal {goal.timesPerPeriod})
      </p>
    </li>
  );
}
