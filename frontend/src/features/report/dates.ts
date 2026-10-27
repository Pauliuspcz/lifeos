import {
  differenceInCalendarDays,
  differenceInCalendarMonths,
  differenceInCalendarWeeks,
  differenceInCalendarYears,
  parseISO,
} from "date-fns";
import { Goal, GoalPeriod } from "../goals";

export function daysSince(date: string, now = new Date()): number {
  return differenceInCalendarDays(now, parseISO(date));
}

export function lastCheckinLabel(
  lastDate: string | null,
  now = new Date(),
): string {
  if (lastDate === null) return "aldri";
  const days = daysSince(lastDate, now);
  if (days <= 0) return "i dag";
  if (days === 1) return "i går";
  return `${days} dager siden`;
}

export function periodsSince(
  period: GoalPeriod,
  start: string,
  now = new Date(),
): number {
  const from = parseISO(start);
  switch (period) {
    case "DAILY":
      return differenceInCalendarDays(now, from) + 1;
    case "WEEKLY":
      return differenceInCalendarWeeks(now, from, { weekStartsOn: 1 }) + 1;
    case "MONTHLY":
      return differenceInCalendarMonths(now, from) + 1;
    case "YEARLY":
      return differenceInCalendarYears(now, from) + 1;
  }
}

export function averagePerPeriod(
  totalSessions: number,
  goal: Goal,
  now = new Date(),
): number {
  return totalSessions / periodsSince(goal.period, goal.createdAt, now);
}
