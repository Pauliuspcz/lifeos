export type GoalPeriod = "DAILY" | "WEEKLY" | "MONTHLY" | "YEARLY";
export type GoalStatus = "ACTIVE" | "ARCHIVED";

export type Goal = {
    goalId: number;
    title: string;
    description: string | null;
    period: GoalPeriod;
    timesPerPeriod: number;
    amountPerSession: number | null;
    status: GoalStatus;
}

export type CreateGoalRequest = {
    title: string;
    description: string | null;
    period: GoalPeriod;
    timesPerPeriod: number;
    amountPerSession: number | null;
}