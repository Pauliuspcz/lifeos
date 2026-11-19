import { request } from "../../shared/api/client";
import { reportEndpoints } from "./endpoints";
import type { ActivityReport, DayLog, GoalReport, Wellbeing } from "./types";

export const getGoalReport = (goalId: number) =>
  request<GoalReport>("GET", reportEndpoints.goal(goalId), undefined, true);

export const getActivity = () =>
  request<ActivityReport>("GET", reportEndpoints.activity, undefined, true);

export const getDayLogToday = () =>
  request<DayLog>("GET", reportEndpoints.dayLogToday, undefined, true);

export const saveDayLog = (change: { mood?: number; sleepHours?: number }) =>
  request<DayLog>("PUT", reportEndpoints.dayLog, change, true);

export const getWellbeing = () =>
  request<Wellbeing>("GET", reportEndpoints.dayLogSummary, undefined, true);
