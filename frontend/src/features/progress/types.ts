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
