import { PERIOD_LABEL, type Goal, type TrackingType } from "./types";

export type FieldKey =
  "period" | "timesPerPeriod" | "amount" | "targetTime" | "targetDate";

export const FIELDS_BY_TRACKING: Record<TrackingType, FieldKey[]> = {
  CHECK: [],
  COUNT: ["period", "timesPerPeriod"],
  AMOUNT: ["amount"],
  DURATION: ["amount"],
  LIMIT: ["amount"],
  TIME_TARGET: ["targetTime"],
  EVENT: ["targetDate"],
};

export function describeGoal(g: Goal): string {
  const per = g.period ? ` per ${PERIOD_LABEL[g.period]}` : "";
  switch (g.trackingType) {
    case "COUNT":
      return `${g.timesPerPeriod ?? 1}×${per}`;
    case "AMOUNT":
    case "DURATION":
      return `${g.amountPerSession ?? ""} ${g.unit ?? ""}${per}`.trim();
    case "LIMIT":
      return `max ${g.amountPerSession ?? ""} ${g.unit ?? ""}${per}`.trim();
    case "TIME_TARGET":
      return `at ${g.targetTime?.slice(0, 5) ?? ""}`;
    case "EVENT":
      return g.targetDate ? `by ${g.targetDate}` : "";
    default:
      return per ? `Daily` : "";
  }
}
