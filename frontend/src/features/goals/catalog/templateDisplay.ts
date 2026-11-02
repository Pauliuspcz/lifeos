import type { Template } from "./types";
import { PERIOD_LABEL } from "../types";

export const DIFFICULTY_LABEL = {
  EASY: "Easy",
  MEDIUM: "Medium",
  HARD: "Hard",
} as const;

export function describeTemplate(t: Template): string {
  const per = t.defaultPeriod ? ` per ${PERIOD_LABEL[t.defaultPeriod]}` : "";
  switch (t.trackingType) {
    case "COUNT":
      return `${t.defaultTimesPerPeriod ?? 1}×${per}`;
    case "AMOUNT":
    case "DURATION":
      return `${t.defaultTargetAmount ?? ""} ${t.defaultUnit ?? ""}${per}`.trim();
    case "LIMIT":
      return `max ${t.defaultTargetAmount ?? ""} ${t.defaultUnit ?? ""}${per}`.trim();
    case "TIME_TARGET":
      return `at ${t.defaultTargetTime?.slice(0, 5) ?? ""}`;
    case "EVENT":
      return "Pick a date";
    default:
      return "Daily";
  }
}
