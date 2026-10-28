import { parseISO, subDays } from "date-fns";
import type { Goal } from "../../goals";
import { periodStart, type Progress } from "../../progress";

function reachedTarget(
  dates: string[],
  start: string,
  end: string | null,
  target: number,
) {
  return (
    dates.filter((d) => d >= start && (end === null || d < end)).length >=
    target
  );
}

export function currentStreak(
  entries: Progress[],
  goal: Goal,
  now = new Date(),
): number {
  const dates = entries.filter((e) => e.goalId === goal.id).map((e) => e.date);
  let start = periodStart(goal.period, now);
  let end: string | null = null;
  let streak = 0;

  if (reachedTarget(dates, start, end, goal.timesPerPeriod)) streak++;

  while (true) {
    end = start;
    start = periodStart(goal.period, subDays(parseISO(end), 1));
    if (!reachedTarget(dates, start, end, goal.timesPerPeriod)) break;
    streak++;
  }
  return streak;
}
