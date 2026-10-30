import { parseISO, subDays } from "date-fns";
import { Goal, periodOf, timesOf } from "../../goals";
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
  const period = periodOf(goal);
  const target = timesOf(goal);
  const dates = entries.filter((e) => e.goalId === goal.id).map((e) => e.date);
  let start = periodStart(period, now);
  let end: string | null = null;
  let streak = 0;

  if (reachedTarget(dates, start, end, target)) streak++;

  while (true) {
    end = start;
    start = periodStart(period, subDays(parseISO(end), 1));
    if (!reachedTarget(dates, start, end, target)) break;
    streak++;
  }
  return streak;
}
