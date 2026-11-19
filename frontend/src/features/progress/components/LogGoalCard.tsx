import type { CSSProperties } from "react";
import { type Goal, PERIOD_LABEL, periodOf, timesOf } from "../../goals";
import type { ProgressEntry, ProgressWindow, TrackingModel } from "../types";
import { dayLabel, daysBetween } from "../utils/days";
import { percent } from "../utils/pace";

type Props = {
  goal: Goal;
  model: TrackingModel;
  win: ProgressWindow;
  entries: ProgressEntry[];
  busy: boolean;
  onToggle: (goalId: number, date: string, entry?: ProgressEntry) => void;
};

const SEGMENT_MAX = 12;

export default function LogGoalCard({
  goal,
  model,
  win,
  entries,
  busy,
  onToggle,
}: Props) {
  const counted = model !== "DAILY_CHECK";
  const range = win.editable[model];
  const chipDays = daysBetween(range.from, range.to);
  const historyDays = daysBetween(win.from, win.today);
  const byDate = new Map(entries.map((e) => [e.date, e]));
  const target = timesOf(goal);
  const periodStart =
    model === "MONTHLY_COUNT" ? win.monthStart : win.weekStart;
  const periodCount = entries.filter((e) => e.date >= periodStart).length;
  const done = counted ? periodCount >= target : byDate.has(win.today);

  const status = counted
    ? `${periodCount}/${target} this ${PERIOD_LABEL[periodOf(goal)]}`
    : done
      ? "Done today"
      : "Not done today";

  return (
    <div
      className="log-card"
      style={{ "--goal-color": goal.color ?? undefined } as CSSProperties}
    >
      <div className="box-header">
        <h2>{goal.title}</h2>
        <span className={"log-status" + (done ? " done" : "")}>
          {done && "✓ "}
          {status}
        </span>
      </div>

      {counted &&
        (target <= SEGMENT_MAX ? (
          <div
            className="segments"
            role="img"
            aria-label={`${periodCount} of ${target}`}
          >
            {Array.from({ length: target }, (_, i) => (
              <span
                key={i}
                className={"segment" + (i < periodCount ? " filled" : "")}
              />
            ))}
          </div>
        ) : (
          <div className="progress-bar">
            <div
              className="progress-bar-fill"
              style={{ width: `${percent(periodCount, target)}%` }}
            />
          </div>
        ))}

      <div className="log-row">
        <div className="day-chips">
          {chipDays.map((d) => {
            const entry = byDate.get(d);
            return (
              <button
                type="button"
                key={d}
                disabled={busy || (counted && done && !entry)}
                aria-pressed={entry !== undefined}
                className={
                  "day-chip" +
                  (entry ? " done" : "") +
                  (d === win.today ? " today" : "")
                }
                onClick={() => onToggle(goal.id, d, entry)}
              >
                {entry && "✓ "}
                {dayLabel(d, win.today, !counted)}
              </button>
            );
          })}
        </div>
        <div className="dots" role="img" aria-label="Last 7 days">
          {historyDays.map((d) => (
            <span
              key={d}
              title={d}
              className={"dot" + (byDate.has(d) ? " done" : "")}
            />
          ))}
        </div>
      </div>
    </div>
  );
}
