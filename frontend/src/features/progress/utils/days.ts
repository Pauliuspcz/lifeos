export function shiftDate(date: string, days: number): string {
  const d = new Date(date + "T00:00:00");
  d.setDate(d.getDate() + days);
  return d.toLocaleDateString("sv-SE");
}

export function daysBetween(from: string, to: string): string[] {
  const out: string[] = [];
  for (let d = from; d <= to; d = shiftDate(d, 1)) out.push(d);
  return out;
}

const WEEKDAYS = ["Su", "Mo", "Tu", "we", "Th", "Fr", "Sa"];

export function dayLabel(
  date: string,
  today: string,
  relative: boolean,
): string {
  if (relative && date === today) return "Today";
  if (relative && date === shiftDate(today, -1)) return "Yesterday";
  return WEEKDAYS[new Date(date + "T00:00:00").getDay()];
}
