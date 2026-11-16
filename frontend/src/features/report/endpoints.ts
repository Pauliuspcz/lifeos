export const reportEndpoints = {
  goal: (goalId: number) => `/api/reports/goals/${goalId}`,
  activity: "/api/reports/activity",
} as const;
