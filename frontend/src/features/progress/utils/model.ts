import type { Goal } from "../../goals";
import type { TrackingModel } from "../types";

export function modelOf(goal: Goal): TrackingModel | null {
  if (goal.trackingType === "AMOUNT") return "CUMULATIVE_SUM";
  if (goal.period === "WEEKLY") return "WEEKLY_COUNT";
  if (goal.period === "MONTHLY") return "MONTHLY_COUNT";
  if (goal.period === "DAILY") return "DAILY_CHECK";
  return null;
}
