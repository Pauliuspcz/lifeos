import type { GoalPeriod, GoalType, TrackingType } from "../types";

export type Category = {
  id: number;
  code: string;
  name: string;
  description: string | null;
  iconKey: string | null;
  color: string | null;
  sortOrder: number;
};

export type Direction = Category;

export type Template = {
  id: number;
  categoryId: number;
  title: string;
  description: string | null;
  goalType: GoalType;
  trackingType: TrackingType;
  defaultPeriod: GoalPeriod | null;
  defaultTimesPerPeriod: number | null;
  defaultTargetAmount: number | null;
  defaultUnit: string | null;
  defaultTargetTime: string | null;
  difficulty: "EASY" | "MEDIUM" | "HARD" | null;
  iconKey: string | null;
  color: string | null;
};
