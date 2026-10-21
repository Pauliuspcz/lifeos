import type { GoalStatus } from "./types";

export const goalEndpoints = {
  collection: "/api/goal",
  list: (status?: GoalStatus) =>
    status ? `/api/goal?status=${status}` : "/api/goal",
  archive: (id: number) => `/api/goal/${id}/archive`,
} as const;
