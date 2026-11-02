import { type Goal, timesOf } from "../../goals";
import type { ProgressEntry, ProgressWindow, TrackingModel } from "../types";
import { dayLabel, daysBetween } from "../utils/days";

type Props = {
  goal: Goal;
  model: TrackingModel;
  win: ProgressWindow;
  entries: ProgressEntry[];
  busy: boolean;
  onToggle: (goalId: number, date: string, entry?: ProgressEntry) => void;
};

export default function LogGoalCard({
  goal,
  model,
  win,
  entries,
  busy,
  onToggle,
}: Props) {
  const weekly = model === "WEEKLY_COUNT";
  const range = win.editable[model];
  const chipDays = daysBetween(range.from, range.to);
  const historyDays = daysBetween(win.from, win.today);
  const byDate = new Map(entries.map((e) => [e.date, e]));
  const target = timesOf(goal);
  const weekCount = entries.filter((e) => e.date >= win.weekStart).length;

  const status = weekly
    ? `${weekCount}/${target} this week` +
      (weekCount >= target ? " · done" : "")
    : byDate.has(win.today)
      ? "Done today"
      : "Not done today";

  return (
    <div className="log-card">
      <div className="box-header">
        <h2>{goal.title}</h2>
        <span className="muted">{status}</span>
      </div>
      {goal.amountPerSession !== null && (
        <p className="muted">
          {goal.amountPerSession} {goal.unit}
        </p>
      )}
      <div className="day-chips">
        {chipDays.map((d) => {
          const entry = byDate.get(d);
          return (
            <button
              type="button"
              key={d}
              disabled={busy}
              aria-pressed={entry !== undefined}
              className={
                "day-chip" +
                (entry ? " done" : "") +
                (d === win.today ? " today" : "")
              }
              onClick={() => onToggle(goal.id, d, entry)}
            >
              {dayLabel(d, win.today, !weekly)}
            </button>
          );
        })}
      </div>
      <div className="dots" aria-label="Last 7 days">
        {historyDays.map((d) => (
          <span
            key={d}
            title={d}
            className={"dot" + (byDate.has(d) ? " done" : "")}
          />
        ))}
      </div>
    </div>
  );
}
