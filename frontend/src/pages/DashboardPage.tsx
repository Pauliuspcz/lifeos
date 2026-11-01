import Topbar from "../shared/components/Topbar";
import { GoalsCard, useGoals } from "../features/goals";
import { CheckinCard, useProgress } from "../features/progress";
import { countInPeriod } from "../features/progress";

const MOODS = [
  { n: 1, word: "Awful" },
  { n: 2, word: "Low" },
  { n: 3, word: "Ok" },
  { n: 4, word: "Good" },
  { n: 5, word: "Great" },
];

const SLEEP = ["≤5", "6", "7", "8", "9+"];

const DAYS = ["Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"];

export default function DashboardPage() {
  const todayIndex = (new Date().getDay() + 6) % 7;
  const { goals, loading, loadError, addGoal, archiveGoal } =
    useGoals("ACTIVE");
  const { entries, addProgress, removeProgress } = useProgress();

  return (
    <>
      <Topbar />
      <div className="dashboard">
        <GoalsCard
          goals={goals}
          loading={loading}
          loadError={loadError}
          onCreate={addGoal}
          onArchive={archiveGoal}
          countFor={(g) => countInPeriod(entries, g)}
        />
        <div className="streak-box">
          <div className="box-header">
            <h2>This week</h2>
          </div>
          <div className="week">
            {DAYS.map((day, i) => (
              <div className="week-day" key={day}>
                <div
                  className={"week-bar" + (i === todayIndex ? " today" : "")}
                />
                <span className="muted">{day}</span>
              </div>
            ))}
          </div>
          <div className="stats">
            <div>
              <span className="muted">Completed</span>
              <strong>–</strong>
            </div>
            <div>
              <span className="muted">Longest streak</span>
              <strong>–</strong>
            </div>
          </div>
        </div>
        <CheckinCard
          goals={goals}
          entries={entries}
          onCheck={addProgress}
          onUncheck={removeProgress}
        />
        <div className="scale-box">
          <h2>Quick log</h2>
          <p className="label">Mood today</p>
          <div className="scale">
            {MOODS.map((m) => (
              <button type="button" className="scale-btn" key={m.n}>
                <strong>{m.n}</strong>
                <span className="muted">{m.word}</span>
              </button>
            ))}
          </div>
          <p className="label">Sleep last night (hours)</p>
          <div className="scale">
            {SLEEP.map((s) => (
              <button type="button" className="scale-btn" key={s}>
                {s}
              </button>
            ))}
          </div>
          <p className="label">One line a day</p>
          <div className="journal">
            <input type="text" placeholder="What went well today?" />
            <button type="button" className="btn-dark">
              Save
            </button>
          </div>
        </div>
      </div>
    </>
  );
}
