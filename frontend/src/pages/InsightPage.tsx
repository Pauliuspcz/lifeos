import Topbar from "../shared/components/Topbar";
import { useGoals } from "../features/goals";
import { GoalInsightCard, useGoalReports } from "../features/report";
import { useProgress } from "../features/progress";

export default function InsightPage() {
  const { goals, loading: goalsLoading } = useGoals("ACTIVE");
  const { reports, loading, loadError } = useGoalReports(goals);
  const isLoading = goalsLoading || loading;
  const { entries } = useProgress();
  return (
    <>
      <Topbar />
      <div className="page">
        <h1>Innsikt</h1>
        {isLoading && <p>Laster…</p>}
        {loadError && <p className="error">{loadError}</p>}
        {!isLoading && !loadError && goals.length === 0 && (
          <p>Ingen mål ennå.</p>
        )}
        <ul>
          {goals.map((g) => (
            <GoalInsightCard
              key={g.id}
              goal={g}
              report={reports.find((r) => r.goalId === g.id)}
              entries={entries}
            />
          ))}
        </ul>
      </div>
    </>
  );
}
