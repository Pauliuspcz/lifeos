import Topbar from "../shared/components/Topbar";
import { useGoals } from "../features/goals";
import {
  GoalInsightCard,
  useGoalReports,
  ActivityCalendar,
  useActivity,
} from "../features/report";

export default function InsightPage() {
  const {
    goals,
    loading: goalsLoading,
    loadError: goalsError,
  } = useGoals("ACTIVE");
  const { reports, loading, loadError } = useGoalReports(goals);
  const { activity, loadError: activityError } = useActivity();
  const isLoading = goalsLoading || loading;
  const statsPending = isLoading || !!goalsError;
  const checkReports = reports.filter(
    (r) => goals.find((g) => g.id === r.goalId)?.trackingType === "CHECK",
  );
  const listStreaks = checkReports.filter((r) => r.currentStreak > 0).length;
  const streakCount = goalsError
    ? activity?.activeStreaks
    : isLoading
      ? undefined
      : listStreaks;
  const avgCompletion =
    checkReports.length === 0
      ? 0
      : Math.round(
          checkReports.reduce((sum, r) => sum + r.successRate, 0) /
            checkReports.length,
        );
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
          <div className="stats insight-stats">
            <div>
              <strong>{activity ? activity.last30Days : "…"}</strong>
              <span className="muted">check-ins last 30 days</span>
            </div>
            <div>
              <strong>{streakCount ?? "…"}</strong>
              <span className="muted">
                active streaks{goalsError ? " · incl. archived" : ""}
              </span>
            </div>
            <div>
              <strong>{statsPending ? "…" : `${avgCompletion}%`}</strong>
              <span className="muted">avg completion</span>
            </div>
          </div>
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
