export type GoalStatus = "ACTIVE" | "ARCHIVED";

export const PERIODS = ["DAILY", "WEEKLY", "MONTHLY", "YEARLY"] as const;
export type GoalPeriod = (typeof PERIODS)[number];

export type Goal = {
  goalId: number;
  title: string;
  description: string | null;
  period: GoalPeriod;
  timesPerPeriod: number;
  amountPerSession: number | null;
  status: GoalStatus;
};

export type CreateGoalRequest = {
  title: string;
  description: string | null;
  period: GoalPeriod;
  timesPerPeriod: number;
  amountPerSession: number | null;
};
