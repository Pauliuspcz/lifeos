import Topbar from "../shared/components/Topbar";
import { GoalsCard, useGoals } from "../features/goals";
import { StreaksCard, useGoalReports } from "../features/report";
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

export default function DashboardPage() {
  const { goals, loading, loadError, addGoal, archiveGoal } =
    useGoals("ACTIVE");
  const { entries, addProgress, removeProgress } = useProgress();
  const { reports } = useGoalReports(goals, entries);

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
        <CheckinCard
          goals={goals}
          entries={entries}
          onCheck={addProgress}
          onUncheck={removeProgress}
        />
        <StreaksCard goals={goals} reports={reports} />
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
