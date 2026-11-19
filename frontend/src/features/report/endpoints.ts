export const reportEndpoints = {
  goal: (goalId: number) => `/api/reports/goals/${goalId}`,
  activity: "/api/reports/activity",
  dayLog: "/api/reports/day-log",
  dayLogToday: "/api/reports/day-log/today",
  dayLogSummary: "/api/reports/day-log/summary",
} as const;
