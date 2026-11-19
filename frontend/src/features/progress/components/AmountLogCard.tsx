import { format, parseISO } from "date-fns";
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
  const summary = useGoalSummary(goal.id, win);
  const periodFrom = summary?.currentPeriod?.from;
  const inPeriod = (d: string) => !periodFrom || d >= periodFrom;
  const days = daysBetween(range.from, range.to).filter(inPeriod);
  const [date, setDate] = useState(win.today);
  const [value, setValue] = useState("");
  const [error, setError] = useState("");

  const unit = goal.unit ? ` ${goal.unit}` : "";
  const logged = entries.filter((e) => e.editable && inPeriod(e.date));
  const periodLabel = summary?.currentPeriod
    ? `${format(parseISO(summary.currentPeriod.from), "MMM d")} – ${format(parseISO(summary.currentPeriod.to), "MMM d")}`
    : null;
  const fill = summary ? percent(summary.total, summary.target) : 0;
  const marker =
    summary && summary.expected !== null
      ? percent(summary.expected, summary.target)
      : null;
  const pace = summary ? describePace(summary) : null;
  const delta =
    summary?.delta != null ? Math.round(summary.delta * 10) / 10 : 0;
  const paceClass = delta > 0 ? " ahead" : delta < 0 ? " behind" : "";

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
        <span className="log-status">
          {summary
            ? formatAmount(summary.total) +
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

      {(periodLabel || pace) && (
        <p className="pace">
          {periodLabel}
          {periodLabel && pace && " · "}
          {pace && <span className={"pace-text" + paceClass}>{pace}</span>}
        </p>
      )}

      <p className="log-label">Log for</p>
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
            {dayLabel(d, win.today, true)}
          </button>
        ))}
      </div>

      <form className="amount-form" onSubmit={submit}>
        <label className="amount-field">
          <input
            type="number"
            inputMode="decimal"
            step="any"
            min="0"
            placeholder="0"
            aria-label={`Amount${goal.unit ? ` in ${goal.unit}` : ""}`}
            value={value}
            onChange={(e) => setValue(e.target.value)}
            disabled={busy}
          />
          {goal.unit && <span className="amount-unit">{goal.unit}</span>}
        </label>
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
                {dayLabel(e.date, win.today, true)} ·{" "}
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
