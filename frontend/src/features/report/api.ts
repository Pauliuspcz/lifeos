import { request } from "../../shared/api/client";
import { reportEndpoints } from "./endpoints";
import type { ActivityReport, GoalReport } from "./types";

export const getGoalReport = (goalId: number) =>
  request<GoalReport>("GET", reportEndpoints.goal(goalId), undefined, true);

export const getActivity = () =>
  request<ActivityReport>("GET", reportEndpoints.activity, undefined, true);
