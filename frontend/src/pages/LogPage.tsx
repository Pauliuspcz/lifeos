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
import AmountLogCard from "../features/progress/components/AmountLogCard";

export default function LogPage() {
  const {
    goals,
    loading: goalsLoading,
    loadError: goalsError,
  } = useGoals("ACTIVE");
  const { data, loading, loadError, logDay, unlogDay } = useProgressWindow();
  const [actionError, setActionError] = useState("");
  const [busy, setBusy] = useState(false);

  async function run(action: () => Promise<void>) {
    setActionError("");
    setBusy(true);
    try {
      await action();
    } catch (err) {
      setActionError((err as Error).message || "Something went wrong");
    } finally {
      setBusy(false);
    }
  }

  const toggle = (goalId: number, date: string, entry?: ProgressEntry) =>
    run(() => (entry ? unlogDay(entry.id) : logDay(goalId, date)));

  const logAmount = (goalId: number, date: string, amount: number) =>
    run(() => logDay(goalId, date, amount));

  const removeEntry = (id: number) => run(() => unlogDay(id));

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
          const goalEntries = data.entries.filter((e) => e.goalId === g.id);
          if (model === "CUMULATIVE_SUM") {
            return (
              <AmountLogCard
                key={g.id}
                goal={g}
                win={data}
                entries={goalEntries}
                busy={busy}
                onLog={logAmount}
                onUnlog={removeEntry}
              />
            );
          }
          return (
            <LogGoalCard
              key={g.id}
              goal={g}
              model={model}
              win={data}
              entries={goalEntries}
              busy={busy}
              onToggle={toggle}
            />
          );
        })}
      </div>
    </>
  );
}
