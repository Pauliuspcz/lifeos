export type GoalStatus = "ACTIVE" | "ARCHIVED";

export const PERIODS = ["DAILY", "WEEKLY", "MONTHLY", "YEARLY"] as const;
export type GoalPeriod = (typeof PERIODS)[number];

export type Goal = {
  id: number;
  title: string;
  description: string | null;
  period: GoalPeriod;
  timesPerPeriod: number;
  amountPerSession: number | null;
  unit: string | null;
  status: GoalStatus;
  templateId: number | null;
  version: number;
  createdAt: string;
  updatedAt: string;
  archivedAt: string | null;
};

export type CreateGoalRequest = {
  title: string;
  description: string | null;
  period: GoalPeriod;
  timesPerPeriod: number;
  amountPerSession: number | null;
};

export const PERIOD_LABEL: Record<GoalPeriod, string> = {
  DAILY: "dag",
  WEEKLY: "uke",
  MONTHLY: "måned",
  YEARLY: "år",
};
