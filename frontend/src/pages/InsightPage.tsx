import Topbar from "../shared/components/Topbar";
import { useGoals } from "../features/goals";
import { useGoalReports } from "../features/report";
import { lastCheckinLabel } from "../features/report/dates";

export default function InsightPage() {
  const { goals, loading: goalsLoading } = useGoals("ACTIVE");
  const { reports, loading, loadError } = useGoalReports(goals);

  const isLoading = goalsLoading || loading;

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
          {goals.map((g) => {
            const r = reports.find((r) => r.goalId === g.id);
            return (
              <li key={g.id}>
                {g.title}: {r?.totalSessions ?? 0} ganger totalt, sist sjekket
                inn {lastCheckinLabel(r?.lastDate ?? null)}
              </li>
            );
          })}
        </ul>
      </div>
    </>
  );
}
