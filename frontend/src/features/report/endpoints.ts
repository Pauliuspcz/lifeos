export const reportEndpoints = {
  goal: (goalId: number) => `/api/reports/goals/${goalId}`,
} as const;
