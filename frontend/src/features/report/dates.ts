import { differenceInCalendarDays, parseISO } from "date-fns";

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
