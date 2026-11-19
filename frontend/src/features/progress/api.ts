import { request } from "../../shared/api/client";
import type {
  CreateProgressRequest,
  GoalProgressSummary,
  Progress,
  ProgressWindow,
} from "./types";
import { progressEndpoints } from "./endpoints";

export const listProgress = () =>
  request<Progress[]>("GET", progressEndpoints.collection, undefined, true);

export const createProgress = (data: CreateProgressRequest) =>
  request<Progress>("POST", progressEndpoints.collection, data, true);

export const deleteProgress = (id: number) =>
  request<void>("DELETE", progressEndpoints.byId(id), undefined, true);

export const fetchProgressWindow = () =>
  request<ProgressWindow>("GET", progressEndpoints.window, undefined, true);

export const listProgressForGoal = (goalId: number) =>
  request<Progress[]>(
    "GET",
    progressEndpoints.forGoal(goalId),
    undefined,
    true,
  );

export const fetchGoalSummary = (goalId: number) =>
  request<GoalProgressSummary>(
    "GET",
    progressEndpoints.summary(goalId),
    undefined,
    true,
  );
