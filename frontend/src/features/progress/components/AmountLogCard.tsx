import { useState, type CSSProperties, type FormEvent } from "react";
import type { Goal } from "../../goals";
import { useGoalSummary } from "../hooks/useGoalSummary";
import type { ProgressEntry, ProgressWindow } from "../types";
import { dayLabel, daysBetween } from "../utils/days";
import { describePace, formatAmount, percent } from "../utils/pace";

type Props = {
  goal: Goal;
  win: ProgressWindow;
  entries: ProgressEntry[];
  busy: boolean;
  onLog: (goalId: number, date: string, amount: number) => Promise<void>;
  onUnlog: (entryId: number) => Promise<void>;
};

export default function AmountLogCard({
  goal,
  win,
  entries,
  busy,
  onLog,
  onUnlog,
}: Props) {
  const range = win.editable.CUMULATIVE_SUM;
  const days = daysBetween(range.from, range.to);
  const summary = useGoalSummary(goal.id, win);
  const [date, setDate] = useState(win.today);
  const [value, setValue] = useState("");
  const [error, setError] = useState("");

  const unit = goal.unit ? ` ${goal.unit}` : "";
  const logged = entries.filter((e) => e.editable);
  const fill = summary ? percent(summary.total, summary.target) : 0;
  const marker =
    summary && summary.expected !== null
      ? percent(summary.expected, summary.target)
      : null;
  const pace = summary ? describePace(summary) : null;

  async function submit(e: FormEvent) {
    e.preventDefault();
    const amount = Math.round(Number(value.replace(",", ".")) * 100) / 100;
    if (!Number.isFinite(amount) || amount <= 0) {
      setError("Enter an amount above 0");
      return;
    }
    setError("");
    await onLog(goal.id, date, amount);
    setValue("");
  }

  return (
    <div
      className="log-card"
      style={{ "--goal-color": goal.color ?? undefined } as CSSProperties}
    >
      <div className="box-header">
        <h2>{goal.title}</h2>
        <span className="muted">
          {summary
            ? `${formatAmount(summary.total)}` +
              (summary.target !== null
                ? ` / ${formatAmount(summary.target)}`
                : "") +
              unit
            : "Loading..."}
        </span>
      </div>

      <div
        className="progress-bar"
        role="progressbar"
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={Math.round(fill)}
      >
        <div className="progress-bar-fill" style={{ width: `${fill}%` }} />
        {marker !== null && (
          <div
            className="progress-marker"
            style={{ left: `${marker}%` }}
            title="Planned pace"
          />
        )}
      </div>
      {pace && <p className="muted">{pace}</p>}

      <div className="day-chips">
        {days.map((d) => (
          <button
            type="button"
            key={d}
            disabled={busy}
            aria-pressed={d === date}
            className={
              "day-chip" +
              (d === date ? " selected" : "") +
              (d === win.today ? " today" : "")
            }
            onClick={() => setDate(d)}
          >
            {dayLabel(d, win.today, false)}
          </button>
        ))}
      </div>

      <form className="amount-form" onSubmit={submit}>
        <input
          type="number"
          inputMode="decimal"
          step="any"
          min="0"
          placeholder={goal.unit ?? "Amount"}
          aria-label={`Amount${goal.unit ? ` in ${goal.unit}` : ""}`}
          value={value}
          onChange={(e) => setValue(e.target.value)}
          disabled={busy}
        />
        <button type="submit" className="btn-dark" disabled={busy}>
          Add
        </button>
      </form>
      {error && <p className="error">{error}</p>}

      {logged.length > 0 && (
        <ul className="amount-list">
          {logged.map((e) => (
            <li key={e.id}>
              <span>
                {dayLabel(e.date, win.today, false)} ·{" "}
                {formatAmount(e.amount ?? 0)}
                {unit}
              </span>
              <button
                type="button"
                className="link-btn"
                disabled={busy}
                onClick={() => void onUnlog(e.id)}
              >
                Remove
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
