import { useState } from "react";
import { Link } from "react-router";
import Topbar from "../shared/components/Topbar";
import { GOAL_PATH } from "../shared/config/routes";
import { useGoals, type Goal } from "../features/goals";
import {
  LogGoalCard,
  modelOf,
  useProgressWindow,
  type ProgressEntry,
  type TrackingModel,
} from "../features/progress";
import AmountLogCard from "../features/progress/components/AmountLogCard";

const SECTIONS: { model: TrackingModel; title: string }[] = [
  { model: "DAILY_CHECK", title: "Daily" },
  { model: "WEEKLY_COUNT", title: "Weekly" },
  { model: "MONTHLY_COUNT", title: "Monthly" },
  { model: "CUMULATIVE_SUM", title: "Totals" },
];

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

  const dailyGoals = goals.filter((g) => modelOf(g) === "DAILY_CHECK");
  const dailyDone = data
    ? dailyGoals.filter((g) =>
        data.entries.some((e) => e.goalId === g.id && e.date === data.today),
      ).length
    : 0;
  const unsupported = goals.filter((g) => modelOf(g) === null);

  function renderCard(g: Goal, model: TrackingModel) {
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
  }

  function renderSection({ model, title }: (typeof SECTIONS)[number]) {
    const items = goals.filter((g) => modelOf(g) === model);
    if (items.length === 0) return null;
    return (
      <section className="log-section" key={model}>
        <div className="log-section-head">
          <h2>{title}</h2>
          {model === "DAILY_CHECK" && data && (
            <span className="muted">
              {dailyDone}/{items.length} done today
            </span>
          )}
        </div>
        {items.map((g) => renderCard(g, model))}
      </section>
    );
  }

  return (
    <>
      <Topbar />
      <div className="page page-wide">
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

        <div className="log-columns">
          <div className="log-col">{renderSection(SECTIONS[0])}</div>
          <div className="log-col">
            {SECTIONS.slice(1).map((s) => renderSection(s))}
            {unsupported.length > 0 && (
              <section className="log-section">
                <div className="log-section-head">
                  <h2>Other</h2>
                </div>
                {unsupported.map((g) => (
                  <div className="log-card" key={g.id}>
                    <h2>{g.title}</h2>
                    <p className="muted">Not supported yet.</p>
                  </div>
                ))}
              </section>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
