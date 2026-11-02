import { useState } from "react";
import { Link } from "react-router";
import Topbar from "../shared/components/Topbar";
import { GOAL_PATH } from "../shared/config/routes";
import { useGoals } from "../features/goals";
import {
  LogGoalCard,
  modelOf,
  useProgressWindow,
  type ProgressEntry,
} from "../features/progress";

export default function LogPage() {
  const {
    goals,
    loading: goalsLoading,
    loadError: goalsError,
  } = useGoals("ACTIVE");
  const { data, loading, loadError, logDay, unlogDay } = useProgressWindow();
  const [actionError, setActionError] = useState("");
  const [busy, setBusy] = useState(false);

  async function toggle(goalId: number, date: string, entry?: ProgressEntry) {
    setActionError("");
    setBusy(true);
    try {
      if (entry) await unlogDay(entry.id);
      else await logDay(goalId, date);
    } catch (err) {
      setActionError((err as Error).message || "Something went wrong");
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <Topbar />
      <div className="page">
        <h1>Log</h1>
        {(goalsLoading || loading) && <p>Loading...</p>}
        {goalsError && <p className="error">{goalsError}</p>}
        {loadError && <p className="error">{loadError}</p>}
        {actionError && <p className="error">{actionError}</p>}
        {!goalsLoading && !goalsError && goals.length === 0 && (
          <p>
            No active goals yet. <Link to={GOAL_PATH}>Create your first</Link>.
          </p>
        )}
        {goals.map((g) => {
          const model = modelOf(g);
          if (model === null) {
            return (
              <div className="log-card" key={g.id}>
                <h2>{g.title}</h2>
                <p className="muted">Not supported yet.</p>
              </div>
            );
          }
          if (!data) {
            return (
              <div className="log-card" key={g.id}>
                <h2>{g.title}</h2>
              </div>
            );
          }
          return (
            <LogGoalCard
              key={g.id}
              goal={g}
              model={model}
              win={data}
              entries={data.entries.filter((e) => e.goalId === g.id)}
              busy={busy}
              onToggle={toggle}
            />
          );
        })}
      </div>
    </>
  );
}
