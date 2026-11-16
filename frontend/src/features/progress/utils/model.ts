//Todo: dette er temp fiks til logging-runden (månedlige og amount-mål)
import type { Goal } from "../../goals";
import type { TrackingModel } from "../types";

export function modelOf(goal: Goal): TrackingModel | null {
  if (goal.trackingType !== "CHECK") return null;
  if (goal.period === "WEEKLY") return "WEEKLY_COUNT";
  if (goal.period === "DAILY") return "DAILY_CHECK";
  return null;
}
