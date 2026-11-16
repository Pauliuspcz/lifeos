import type { Template } from "./types";
import { PERIOD_LABEL } from "../types";

export const DIFFICULTY_LABEL = {
  EASY: "Easy",
  MEDIUM: "Medium",
  HARD: "Hard",
} as const;

export function describeTemplate(t: Template): string {
  const parts: string[] = [];
  if (t.trackingType === "AMOUNT") {
    const amount =
      `${t.defaultTargetAmount ?? ""} ${t.defaultUnit ?? ""}`.trim();
    parts.push(
      t.defaultPeriod
        ? `${amount} per ${PERIOD_LABEL[t.defaultPeriod]}`
        : `${amount} by a date`,
    );
  } else {
    const period = t.defaultPeriod ?? "DAILY";
    parts.push(
      period === "DAILY"
        ? "Every day"
        : `${t.defaultTimesPerPeriod ?? 1}× per ${PERIOD_LABEL[period]}`,
    );
  }
  if (t.defaultTargetTime) parts.push(`at ${t.defaultTargetTime.slice(0, 5)}`);
  if (t.defaultDurationDays) parts.push(`${t.defaultDurationDays} days`);
  return parts.join(" · ");
}
