import { type Goal, PERIOD_LABEL, periodOf, timesOf } from "../../goals";
import type { CreateProgressRequest, Progress } from "../types";
import { countInPeriod } from "../utils/period";

type Props = {
  goals: Goal[];
  entries: Progress[];
  onCheck: (req: CreateProgressRequest) => Promise<void>;
  onUncheck: (progressId: number) => Promise<void>;
};

export default function CheckinCard({
  goals,
  entries,
  onCheck,
  onUncheck,
}: Props) {
  const today = new Date().toLocaleDateString("sv-SE");

  // Midlertidig: amount-mål får tallinput i logging-runden
  const checkGoals = goals.filter((g) => g.trackingType === "CHECK");

  const entryToday = (goalId: number) =>
    entries.find((e) => e.goalId === goalId && e.date === today);

  async function handleToggle(goal: Goal) {
    const entry = entryToday(goal.id);
    try {
      if (entry) {
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
      {checkGoals.length === 0 && <p className="muted">No habits today.</p>}
      {checkGoals.map((g) => {
        const done = entryToday(g.id) !== undefined;
        const count = countInPeriod(entries, g);
        return (
          <button
            type="button"
            key={g.id}
            className={"habit-row" + (done ? " done" : "")}
            aria-pressed={done}
            onClick={() => handleToggle(g)}
          >
            <span className="check">{done && "✓"}</span>
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
}
