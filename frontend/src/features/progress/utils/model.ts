//Todo: dette er temp fiks til vi har endra seeding backend
import type { Goal } from "../../goals";
import type { TrackingModel } from "../types";

export function modelOf(goal: Goal): TrackingModel | null {
  if (goal.trackingType === "EVENT") return null;
  return goal.trackingType === "COUNT" ? "WEEKLY_COUNT" : "DAILY_CHECK";
}
