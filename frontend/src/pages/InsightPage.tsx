import Topbar from "../shared/components/Topbar";
import { useGoals } from "../features/goals";
import { GoalInsightCard, useGoalReports } from "../features/report";

export default function InsightPage() {
  const { goals, loading: goalsLoading } = useGoals("ACTIVE");
  const { reports, loading, loadError } = useGoalReports(goals);
  const isLoading = goalsLoading || loading;
  return (
    <>
      <Topbar />
      <div className="page">
        <h1>Insights</h1>
        {isLoading && <p>Loading…</p>}
        {loadError && <p className="error">{loadError}</p>}
        {!isLoading && !loadError && goals.length === 0 && <p>No goals yet.</p>}
        <ul>
          {goals.map((g) => (
            <GoalInsightCard
              key={g.id}
              goal={g}
              report={reports.find((r) => r.goalId === g.id)}
            />
          ))}
        </ul>
      </div>
    </>
  );
}
