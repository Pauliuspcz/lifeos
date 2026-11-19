import type { GoalProgressSummary } from "../types";

export const formatAmount = (n: number) =>
  n.toLocaleString("en-US", { maximumFractionDigits: 1 });

export function describePace(s: GoalProgressSummary): string | null {
  if (s.delta === null) return null;
  const unit = s.unit ? ` ${s.unit}` : "";
  const rounded = Math.round(s.delta * 10) / 10;
  if (rounded === 0) return "On pace";
  return rounded > 0
    ? `${formatAmount(rounded)}${unit} ahead of plan`
    : `${formatAmount(-rounded)}${unit} behind plan`;
}

export function percent(part: number, whole: number | null): number {
  if (!whole || whole <= 0) return 0;
  return Math.min(100, Math.max(0, (part / whole) * 100));
}
