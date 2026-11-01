import Topbar from "../shared/components/Topbar";
import { useGoals } from "../features/goals";
import { GoalInsightCard, useGoalReports } from "../features/report";

export default function InsightPage() {
  const { goals, loading: goalsLoading } = useGoals("ACTIVE");
  const { reports, loading, loadError } = useGoalReports(goals);
  const isLoading = goalsLoading || loading;
  const last30Total = reports.reduce((sum, r) => sum + r.last30Days, 0);
  const activeStreaks = reports.filter((r) => r.currentStreak > 0).length;
  const avgCompletion =
    reports.length === 0
      ? 0
      : Math.round(
          reports.reduce((sum, r) => sum + r.successRate, 0) / reports.length,
        );
  return (
    <>
      <Topbar />
      <div className="page">
        {isLoading && <p>Loading…</p>}
        {loadError && <p className="error">{loadError}</p>}
        {!isLoading && !loadError && goals.length === 0 && <p>No goals yet.</p>}
        <div className="insight-grid">
          <section className="activity">
            <h2>Activity</h2>
            <p className="muted">Contribution calendar coming soon.</p>
          </section>
          <div className="stats insight-stats">
            <div>
              <strong>{last30Total}</strong>
              <span className="muted">check-ins last 30 days</span>
            </div>
            <div>
              <strong>{activeStreaks}</strong>
              <span className="muted">active streaks</span>
            </div>
            <div>
              <strong>{avgCompletion}%</strong>
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
      </div>
    </>
  );
}
