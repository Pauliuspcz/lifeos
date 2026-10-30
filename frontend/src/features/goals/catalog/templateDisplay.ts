import type { Template } from "./types";
import { PERIOD_LABEL } from "../types";

export const DIFFICULTY_LABEL = {
  EASY: "Lett",
  MEDIUM: "Middels",
  HARD: "Vanskelig",
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
      return `maks ${t.defaultTargetAmount ?? ""} ${t.defaultUnit ?? ""}${per}`.trim();
    case "TIME_TARGET":
      return `kl. ${t.defaultTargetTime?.slice(0, 5) ?? ""}`;
    case "EVENT":
      return "Velg dato";
    default:
      return "Daglig";
  }
}
