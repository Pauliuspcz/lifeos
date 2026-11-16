export type GoalStatus = "ACTIVE" | "ARCHIVED";

export const PERIODS = ["DAILY", "WEEKLY", "MONTHLY", "YEARLY"] as const;
export type GoalPeriod = (typeof PERIODS)[number];

export const TRACKING_TYPES = ["CHECK", "AMOUNT"] as const;
export type TrackingType = (typeof TRACKING_TYPES)[number];

export type Goal = {
  id: number;
  title: string;
  description: string | null;
  trackingType: TrackingType;
  period: GoalPeriod | null;
  timesPerPeriod: number | null;
  targetAmount: number | null;
  unit: string | null;
  targetTime: string | null; // "HH:mm:ss"
  startDate: string | null;
  targetDate: string | null;
  outdoor: boolean;
  motivation: string | null;
  iconKey: string | null;
  color: string | null;
  categoryId: number;
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
  trackingType?: TrackingType;
  period?: GoalPeriod | null;
  timesPerPeriod?: number | null;
  targetAmount?: number | null;
  unit?: string | null;
  targetTime?: string | null;
  startDate?: string | null;
  targetDate?: string | null;
  categoryId: number;
  templateId?: number | null;
  motivation?: string | null;
  iconKey?: string | null;
  color?: string | null;
  outdoor?: boolean;
};

export const PERIOD_LABEL: Record<GoalPeriod, string> = {
  DAILY: "day",
  WEEKLY: "week",
  MONTHLY: "month",
  YEARLY: "year",
};

// Midlertidig: progress/report forstår bare periodebaserte mål foreløpig
export const periodOf = (g: Pick<Goal, "period">): GoalPeriod =>
  g.period ?? "DAILY";
export const timesOf = (g: Pick<Goal, "timesPerPeriod">): number =>
  g.timesPerPeriod ?? 1;
