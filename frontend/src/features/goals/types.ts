export type GoalStatus = "ACTIVE" | "ARCHIVED";

export const PERIODS = ["DAILY", "WEEKLY", "MONTHLY", "YEARLY"] as const;

export type GoalPeriod = (typeof PERIODS)[number];

export const TRACKING_TYPES = [
  "CHECK",
  "COUNT",
  "AMOUNT",
  "DURATION",
  "TIME_TARGET",
  "LIMIT",
  "EVENT",
] as const;
export type TrackingType = (typeof TRACKING_TYPES)[number];

export const GOAL_TYPES = ["HABIT", "TARGET", "EVENT", "OTHER"] as const;
export type GoalType = (typeof GOAL_TYPES)[number];

export type Goal = {
  id: number;
  title: string;
  description: string | null;
  goalType: GoalType;
  trackingType: TrackingType;
  period: GoalPeriod | null;
  timesPerPeriod: number | null;
  amountPerSession: number | null;
  unit: string | null;
  targetTime: string | null; // "HH:mm:ss"
  startDate: string | null;
  targetDate: string | null;
  motivation: string | null;
  iconKey: string | null;
  color: string | null;
  categoryId: number | null;
  templateId: number | null;
  status: GoalStatus;
  version: number;
  createdAt: string;
  updatedAt: string;
  archivedAt: string | null;
};

export type CreateGoalRequest = {
  title: string;
  description?: string | null;
  goalType?: GoalType;
  trackingType?: TrackingType;
  period?: GoalPeriod | null;
  timesPerPeriod?: number | null;
  amountPerSession?: number | null;
  unit?: string | null;
  targetTime?: string | null;
  startDate?: string | null;
  targetDate?: string | null;
  categoryId?: number | null;
  templateId?: number | null;
  motivation?: string | null;
  iconKey?: string | null;
  color?: string | null;
};

export const PERIOD_LABEL: Record<GoalPeriod, string> = {
  DAILY: "dag",
  WEEKLY: "uke",
  MONTHLY: "måned",
  YEARLY: "år",
};

// Midlertidig: progress/report forstår bare periodebaserte mål (COUNT) foreløpig
export const periodOf = (g: Pick<Goal, "period">): GoalPeriod =>
  g.period ?? "DAILY";
export const timesOf = (g: Pick<Goal, "timesPerPeriod">): number =>
  g.timesPerPeriod ?? 1;
