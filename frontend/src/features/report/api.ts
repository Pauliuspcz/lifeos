import { request } from "../../shared/api/client";
import { reportEndpoints } from "./endpoints";
import type { GoalReport } from "./types";

export const getGoalReport = (goalId: number) =>
  request<GoalReport>("GET", reportEndpoints.goal(goalId), undefined, true);
