export const progressEndpoints = {
  collection: "/api/progress",
  window: "/api/progress/window",
  byId: (id: number) => `/api/progress/${id}`,
  forGoal: (goalId: number) => `/api/progress?goalId=${goalId}`,
  summary: (goalId: number) => `/api/progress/summary?goalId=${goalId}`,
} as const;
