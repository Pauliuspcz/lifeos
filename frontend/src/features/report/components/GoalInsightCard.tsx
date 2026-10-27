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

export default function GoalInsightCard({ goal, report, entries }: Props) {
  const unit = PERIOD_LABEL[goal.period];
  const total = report?.totalSessions ?? 0;
  const average = formatAverage(averagePerPeriod(total, goal));
  const streak = currentStreak(entries, goal);
  const streakUnit =
    streak === 1 ? PERIOD_LABEL[goal.period] : PERIOD_PLURAL[goal.period];

  return (
    <li>
      <h3>{goal.title}</h3>
      <p>
        {streak > 0 ? `${streak} ${streakUnit} på rad` : "Ingen streak ennå"}
      </p>
      <p>Innsjekker: {total}</p>
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
