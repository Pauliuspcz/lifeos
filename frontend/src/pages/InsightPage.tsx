import Topbar from "../shared/components/Topbar";
import { useCategories, useGoals } from "../features/goals";
import {
  GoalInsightCard,
  useGoalReports,
  ActivityCalendar,
  Last30Card,
  useActivity,
  useWellbeing,
} from "../features/report";

export default function InsightPage() {
  const {
    goals,
    loading: goalsLoading,
    loadError: goalsError,
  } = useGoals("ACTIVE");
  const { reports, loading, loadError } = useGoalReports(goals);
  const { activity, loadError: activityError } = useActivity();
  const { wellbeing } = useWellbeing();
  const categories = useCategories();
  const isLoading = goalsLoading || loading;
  const streakGoals = goals.filter(
    (g) =>
      g.trackingType === "CHECK" &&
      (reports.find((r) => r.goalId === g.id)?.currentStreak ?? 0) > 0,
  );
  const streakCount = goalsError
    ? activity?.activeStreaks
    : isLoading
      ? undefined
      : streakGoals.length;
  return (
    <>
      <Topbar />
      <div className="page">
        {isLoading && <p>Loading…</p>}
        {loadError && <p className="error">{loadError}</p>}
        {!isLoading && !loadError && !goalsError && goals.length === 0 && (
          <p>No goals yet.</p>
        )}
        <div className="insight-grid">
          <ActivityCalendar activity={activity} loadError={activityError} />
          <Last30Card
            activity={activity}
            wellbeing={wellbeing}
            streakGoals={streakGoals}
            streakCount={streakCount}
            categories={categories}
            goalsError={goalsError}
          />
          {goals.map((g) => (
            <GoalInsightCard
              key={g.id}
              goal={g}
              report={reports.find((r) => r.goalId === g.id)}
            />
          ))}
        </div>
        {goalsError && <p className="error">{goalsError}</p>}
      </div>
    </>
  );
}
