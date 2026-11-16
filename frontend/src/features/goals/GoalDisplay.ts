import { PERIOD_LABEL, type Goal } from "./types";

export function describeGoal(g: Goal): string {
  if (g.trackingType === "AMOUNT") {
    const amount = `${g.targetAmount ?? ""} ${g.unit ?? ""}`.trim();
    if (g.period) return `${amount} per ${PERIOD_LABEL[g.period]}`;
    return g.targetDate ? `${amount} by ${g.targetDate}` : amount;
  }
  const base =
    !g.period || g.period === "DAILY"
      ? "Every day"
      : `${g.timesPerPeriod ?? 1}× per ${PERIOD_LABEL[g.period]}`;
  return g.targetDate ? `${base} until ${g.targetDate}` : base;
}
