import { Goal, GoalPeriod, PERIOD_LABEL } from "../../goals";
import { GoalReport } from "../types";
import { averagePerPeriod, lastCheckinLabel } from "../utils/dates";
import { Progress } from "../../progress";
import { currentStreak } from "../utils/streak";

type Props = {
  goal: Goal;
  report?: GoalReport;
  entries: Progress[];
};

const PERIOD_PLURAL: Record<GoalPeriod, string> = {
  //TODO: move to goals/types.ts, unsure of changes by kriss917.
  DAILY: "dager",
  WEEKLY: "uker",
  MONTHLY: "måneder",
  YEARLY: "år",
};

function formatAverage(value: number): string {
  return value.toLocaleString("nb-NO", {
    maximumFractionDigits: 1,
  });
}

function trendLabel(diff: number): string {
  if (diff > 0) return `Opp ${diff} fra forrige 30`;
  if (diff < 0) return `Ned ${Math.abs(diff)} fra forrige 30`;
  return "likt som forrige 30";
}

export default function GoalInsightCard({ goal, report, entries }: Props) {
  const unit = PERIOD_LABEL[goal.period];
  const last30 = report?.last30Days ?? 0;
  const diff = last30 - (report?.previous30Days ?? 0);
  const total = report?.totalSessions ?? 0;
  const average = formatAverage(averagePerPeriod(total, goal));
  const streak = currentStreak(entries, goal);
  const streakUnit =
    streak === 1 ? PERIOD_LABEL[goal.period] : PERIOD_PLURAL[goal.period];

  return (
    <li>
      <h3>{goal.title}</h3>
      <p>Innsjekker: {total}</p>
      <p>
        Siste 30 dager: {last30} ({trendLabel(diff)})
      </p>
      <p>
        {streak > 0 ? `${streak} ${streakUnit} på rad` : "Ingen streak ennå"}
      </p>
      <p>
        Sist sjekket inn:
        {lastCheckinLabel(report?.lastDate ?? null)}
      </p>
      <p>
        Snitt: {average} per {unit} (mål {goal.timesPerPeriod})
      </p>
    </li>
  );
}
