export { PERIOD_LABEL, PERIODS } from "./types";
export type { CreateGoalRequest, Goal, GoalPeriod, GoalStatus } from "./types";
export { default as GoalsCard } from "./components/GoalsCard";
export { useGoals } from "./hooks/useGoals";

export { useAreas } from "./catalog/hooks/useAreas";
export type { Category } from "./catalog/types";
//Midlertidig
export { periodOf, timesOf } from "./types";
