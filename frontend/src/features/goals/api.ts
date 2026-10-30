import { request } from "../../shared/api/client";
import { goalEndpoints } from "./endpoints";
import type { CreateGoalRequest, Goal, GoalStatus } from "./types";

export const listGoals = (status?: GoalStatus) =>
  request<Goal[]>("GET", goalEndpoints.list(status), undefined, true);

export const createGoal = (data: CreateGoalRequest) =>
  request<Goal>("POST", goalEndpoints.collection, data, true);

export const archiveGoal = (id: number) =>
  request<Goal>("PATCH", goalEndpoints.archive(id), undefined, true);

export const unarchiveGoal = (id: number) =>
  request<Goal>("PATCH", goalEndpoints.unarchive(id), undefined, true);

export const deleteGoal = (id: number) =>
  request<void>("DELETE", goalEndpoints.item(id), undefined, true);
