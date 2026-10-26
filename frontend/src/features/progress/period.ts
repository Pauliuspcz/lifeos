import { GoalPeriod } from "../goals";

export function periodStart(period: GoalPeriod, now = new Date()): string {
  let start: Date;
  switch (period) {
    case "DAILY":
      start = new Date(now);
      break;
    case "WEEKLY": {
      const daysSinceMonday = (now.getDay() + 6) % 7;
      start = new Date(now);
      start.setDate(now.getDate() - daysSinceMonday);
      break;
    }
    case "MONTHLY":
      start = new Date(now.getFullYear(), now.getMonth(), 1);
      break;
    case "YEARLY":
      start = new Date(now.getFullYear(), 0, 1);
      break;
  }
  return start.toLocaleDateString("sv-SE");
}
