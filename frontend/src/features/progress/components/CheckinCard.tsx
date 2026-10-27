import { type Goal, PERIOD_LABEL } from "../../goals";
import type { CreateProgressRequest, Progress } from "../types";
import { countInPeriod } from "../period";

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
          amount: goal.amountPerSession,
        });
      }
    } catch (err) {
      alert((err as Error).message);
    }
  }

  return (
    <div className="checkin-box">
      <div className="box-header">
        <h2>Vaner i dag</h2>
        <span className="muted">Trykk for å hake av</span>
      </div>
      {goals.length === 0 && <p className="muted">Ingen vaner i dag.</p>}
      {goals.map((g) => {
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
              {g.amountPerSession !== null && (
                <span className="muted">
                  {g.amountPerSession} {g.unit}
                </span>
              )}
            </span>
            <span className="muted">
              {count}/{g.timesPerPeriod} per {PERIOD_LABEL[g.period]}
            </span>
          </button>
        );
      })}
    </div>
  );
}
