export type GoalReport = {
  goalId: number;
  totalSessions: number;
  lastDate: string | null;
  last30Days: number;
  previous30Days: number;
  currentStreak: number;
  successRate: number;
  history: PeriodCount[];
  suggestion: Suggestion;
};

export type PeriodCount = {
  periodStart: string;
  count: number;
  reached: boolean;
};

export type Recommendation = "RAISE" | "KEEP" | "LOWER" | "NONE" | "INACTIVE";

export type Suggestion = {
  recommendation: Recommendation;
  suggestedTarget: number | null;
};
