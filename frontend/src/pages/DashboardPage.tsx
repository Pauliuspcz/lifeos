import Topbar from "../shared/components/Topbar";
import { GoalsCard, useGoals } from "../features/goals";
import { CheckinCard, useProgress } from "../features/progress";

const MOODS = [
  { n: 1, word: "Tung" },
  { n: 2, word: "Lav" },
  { n: 3, word: "Ok" },
  { n: 4, word: "Bra" },
  { n: 5, word: "Topp" },
];

const SLEEP = ["≤5", "6", "7", "8", "9+"];

const DAYS = ["Ma", "Ti", "On", "To", "Fr", "Lø", "Sø"];

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
        />
        <div className="streak-box">
          <div className="box-header">
            <h2>Denne uka</h2>
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
              <span className="muted">Fullført</span>
              <strong>–</strong>
            </div>
            <div>
              <span className="muted">Lengste streak</span>
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
          <h2>Logg kjapt</h2>
          <p className="label">Humør i dag</p>
          <div className="scale">
            {MOODS.map((m) => (
              <button type="button" className="scale-btn" key={m.n}>
                <strong>{m.n}</strong>
                <span className="muted">{m.word}</span>
              </button>
            ))}
          </div>
          <p className="label">Søvn i natt (timer)</p>
          <div className="scale">
            {SLEEP.map((s) => (
              <button type="button" className="scale-btn" key={s}>
                {s}
              </button>
            ))}
          </div>
          <p className="label">En linje om dagen</p>
          <div className="journal">
            <input type="text" placeholder="Hva gikk bra i dag?" />
            <button type="button" className="btn-dark">
              Lagre
            </button>
          </div>
        </div>
      </div>
    </>
  );
}
