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

export type ActivityDay = {
  date: string;
  count: number;
  level: number;
};

export type ActivityReport = {
  totalCheckins: number;
  activeDays: number;
  last30Days: number;
  activeStreaks: number;
  days: ActivityDay[];
};

export type DayLog = {
  date: string;
  mood: number | null;
  sleepHours: number | null;
};

export type Wellbeing = {
  avgMood: number | null;
  avgSleep: number | null;
  daysLogged: number;
};
