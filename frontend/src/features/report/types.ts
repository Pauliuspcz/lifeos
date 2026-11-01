export type GoalReport = {
  goalId: number;
  totalSessions: number;
  lastDate: string | null;
  last30Days: number;
  previous30Days: number;
  currentStreak: number;
};
