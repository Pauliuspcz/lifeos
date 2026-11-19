import type { GoalPeriod, TrackingType } from "../types";

export type Category = {
  id: number;
  code: string;
  name: string;
  description: string | null;
  iconKey: string | null;
  color: string | null;
  sortOrder: number;
};

export type Direction = Category & {
  categoryId: number;
  iconKey: string | null;
};

export type Difficulty = "EASY" | "MEDIUM" | "HARD";

export type Template = {
  id: number;
  code: string;
  categoryId: number;
  directionId: number;
  title: string;
  description: string | null;
  trackingType: TrackingType;
  defaultPeriod: GoalPeriod | null;
  defaultTimesPerPeriod: number | null;
  defaultTargetAmount: number | null;
  defaultUnit: string | null;
  defaultTargetTime: string | null;
  defaultDurationDays: number | null;
  outdoor: boolean;
  difficulty: Difficulty | null;
  iconKey: string | null;
  color: string | null;
};
