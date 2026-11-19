import { Check } from "lucide-react";
import type { CSSProperties } from "react";
import {
  type Category,
  type Goal,
  PERIOD_LABEL,
  periodOf,
  timesOf,
} from "../../goals";
import type { CreateProgressRequest, Progress } from "../types";
import { countInPeriod, periodStart } from "../utils/period";

type Props = {
  goals: Goal[];
  categories: Category[];
  entries: Progress[];
  onCheck: (req: CreateProgressRequest) => Promise<void>;
  onUncheck: (progressId: number) => Promise<void>;
  loadError: string;
};

export default function CheckinCard({
  goals,
  categories,
  entries,
  onCheck,
  onUncheck,
  loadError,
}: Props) {
  const today = new Date().toLocaleDateString("sv-SE");

  // Midlertidig: amount-mål får tallinput i logging-runden
  const checkGoals = goals.filter((g) => g.trackingType === "CHECK");

  const entryToday = (goalId: number) =>
    entries.find((e) => e.goalId === goalId && e.date === today);

  function latestInPeriod(goal: Goal) {
    const start = periodStart(periodOf(goal));
    return entries
      .filter((e) => e.goalId === goal.id && e.date >= start)
      .sort((a, b) => b.date.localeCompare(a.date))[0];
  }

  async function handleToggle(goal: Goal, done: boolean) {
    const entry = entryToday(goal.id) ?? latestInPeriod(goal);
    try {
      if (done && entry) {
        await onUncheck(entry.id);
      } else {
        await onCheck({
          goalId: goal.id,
          date: today,
          amount: null,
        });
      }
    } catch (err) {
      alert((err as Error).message);
    }
  }

  return (
    <div className="checkin-box">
      <div className="box-header">
        <h2>Today's checkins</h2>
        <span className="muted">Click to check off</span>
      </div>
      {loadError && <p className="muted">Could not load active goals</p>}
      <div className="area-grid">
        {categories.map((c) => {
          const areaGoals = checkGoals.filter((g) => g.categoryId === c.id);
          return (
            <div
              className="area-card"
              key={c.id}
              style={{ "--area-color": c.color ?? undefined } as CSSProperties}
            >
              <h3>{c.name}</h3>
              <p className="muted">
                {areaGoals.length === 0
                  ? "No active goals yet"
                  : `${areaGoals.length} active goal${areaGoals.length === 1 ? "" : "s"}`}
              </p>
              {areaGoals.map((g) => {
                const count = countInPeriod(entries, g);
                const done =
                  entryToday(g.id) !== undefined || count >= timesOf(g);
                return (
                  <button
                    type="button"
                    key={g.id}
                    className={"habit-row" + (done ? " done" : "")}
                    aria-pressed={done}
                    onClick={() => handleToggle(g, done)}
                  >
                    <span className="check">
                      {done && <Check size={12} strokeWidth={3} />}
                    </span>
                    <span className="habit-text">
                      <span className="habit-title">{g.title}</span>
                    </span>
                    <span className="muted">
                      {count}/{timesOf(g)} per {PERIOD_LABEL[periodOf(g)]}
                    </span>
                  </button>
                );
              })}
            </div>
          );
        })}
      </div>
    </div>
  );
}
