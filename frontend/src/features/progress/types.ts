export type Progress = {
  id: number;
  goalId: number;
  date: string;
  amount: number | null;
  createdAt: string;
};

export type CreateProgressRequest = {
  goalId: number;
  date: string;
  amount: number | null;
};

export type TrackingModel = "DAILY_CHECK" | "WEEKLY_COUNT" | "CUMULATIVE_SUM";

export type DateRange = { from: string; to: string };

export type ProgressEntry = {
  id: number;
  goalId: number;
  date: string;
  amount: number | null;
  trackingModel: TrackingModel;
  editable: boolean;
};

export type ProgressWindow = {
  today: string;
  weekStart: string;
  from: string;
  editable: Record<TrackingModel, DateRange>;
  entries: ProgressEntry[];
};
