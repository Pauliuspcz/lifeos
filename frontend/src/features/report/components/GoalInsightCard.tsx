import { Goal, PERIOD_LABEL } from "../../goals";
import { GoalReport } from "../types";
import { averagePerPeriod, lastCheckinLabel } from "../dates";

type Props = {
  goal: Goal;
  report?: GoalReport;
};

function formatAverage(value: number): string {
  return value.toLocaleString("nb-NO", {
    maximumFractionDigits: 1,
  });
}

export default function GoalInsightCard({ goal, report }: Props) {
  const total = report?.totalSessions ?? 0;
  const average = formatAverage(averagePerPeriod(total, goal));

  return (
    <li>
      <h3>{goal.title}</h3>
      <p>Gjennomførte: {total}</p>
      <p>
        Sist sjekket inn:
        {lastCheckinLabel(report?.lastDate ?? null)}
      </p>
      <p>
        Snitt: {average} per {PERIOD_LABEL[goal.period]} (mål{" "}
        {goal.timesPerPeriod})
      </p>
    </li>
  );
}
