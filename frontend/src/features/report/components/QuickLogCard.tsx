import { useEffect, useState } from "react";
import { getDayLogToday, saveDayLog } from "../api";
import type { DayLog } from "../types";

const MOODS = [
  { n: 1, word: "Awful" },
  { n: 2, word: "Low" },
  { n: 3, word: "Ok" },
  { n: 4, word: "Good" },
  { n: 5, word: "Great" },
];

const SLEEP = [
  { hours: 5, label: "≤5" },
  { hours: 6, label: "6" },
  { hours: 7, label: "7" },
  { hours: 8, label: "8" },
  { hours: 9, label: "9+" },
];

export default function QuickLogCard() {
  const [log, setLog] = useState<DayLog | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    getDayLogToday()
      .then((l) => {
        if (!cancelled) setLog(l);
      })
      .catch(() => {
        if (!cancelled) setError("Could not load today's log");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function save(change: { mood?: number; sleepHours?: number }) {
    try {
      setLog(await saveDayLog(change));
      setError("");
    } catch {
      setError("Could not save, try again");
    }
  }

  return (
    <div className="scale-box">
      <h2>Quick log</h2>
      <p className="label">Mood today</p>
      <div className="scale">
        {MOODS.map((m) => (
          <button
            type="button"
            key={m.n}
            className={"scale-btn" + (log?.mood === m.n ? " selected" : "")}
            aria-pressed={log?.mood === m.n}
            onClick={() => save({ mood: m.n })}
          >
            <strong>{m.n}</strong>
            <span className="muted">{m.word}</span>
          </button>
        ))}
      </div>
      <p className="label">Sleep last night (hours)</p>
      <div className="scale">
        {SLEEP.map((s) => (
          <button
            type="button"
            key={s.hours}
            className={
              "scale-btn" + (log?.sleepHours === s.hours ? " selected" : "")
            }
            aria-pressed={log?.sleepHours === s.hours}
            onClick={() => save({ sleepHours: s.hours })}
          >
            {s.label}
          </button>
        ))}
      </div>
      {error && <p className="muted">{error}</p>}
    </div>
  );
}
