import type { GoalStatus } from "./types";

export const goalEndpoints = {
  collection: "/api/goal",
  list: (status?: GoalStatus) =>
    status ? `/api/goal?status=${status}` : "/api/goal",
  item: (id: number) => `/api/goal/${id}`,
  archive: (id: number) => `/api/goal/${id}/archive`,
  unarchive: (id: number) => `/api/goal/${id}/unarchive`,
} as const;
