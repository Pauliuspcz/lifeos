export type GoalReport = {
  goalId: number;
  totalSessions: number;
  lastDate: string | null;
  last30Days: number;
  previous30Days: number;
  currentStreak: number;
  successRate: number;
  history: PeriodCount[];
};

export type PeriodCount = {
  periodStart: string;
  count: number;
  reached: boolean;
};
