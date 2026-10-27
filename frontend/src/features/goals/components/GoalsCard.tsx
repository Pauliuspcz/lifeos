import { CreateGoalRequest, PERIOD_LABEL, Goal } from "../types";
import { useRef } from "react";
import GoalForm from "./GoalForm";

type Props = {
  goals: Goal[];
  loading: boolean;
  loadError: string;
  onCreate: (req: CreateGoalRequest) => Promise<void>;
  onArchive: (goalId: number) => Promise<void>;
  countFor?: (goal: Goal) => number;
};

export default function GoalsCard({
  goals,
  loading,
  loadError,
  onCreate,
  onArchive,
  countFor,
}: Props) {
  const dialogRef = useRef<HTMLDialogElement>(null);

  async function handleArchive(goalId: number) {
    try {
      await onArchive(goalId);
    } catch (err) {
      alert((err as Error).message);
    }
  }

  return (
    <>
      <div className="goals-box">
        <div className="box-header">
          <h2>Mål</h2>
          <button
            type="button"
            className="btn-outline"
            onClick={() => dialogRef.current?.showModal()}
          >
            + Nytt mål
          </button>
        </div>
        {loading && <p className="muted">Laster mål…</p>}
        {loadError && <p className="error">{loadError}</p>}
        {!loading && !loadError && goals.length === 0 && (
          <p className="muted">Ingen mål ennå.</p>
        )}
        {goals.map((g) => (
          <div className="goal-row" key={g.id}>
            <div className="goal-top">
              <span>{g.title}</span>
              <span className="muted">
                {g.timesPerPeriod}× per {PERIOD_LABEL[g.period]}
              </span>
              <button
                type="button"
                className="goal-archive"
                aria-label={"Arkiver " + g.title}
                onClick={() => handleArchive(g.id)}
              >
                ×
              </button>
            </div>
            {countFor && (
              <div className="progress">
                <div
                  className="progress-fill"
                  style={{
                    width: `${Math.min(100, (countFor(g) / g.timesPerPeriod) * 100)}%`,
                  }}
                />
              </div>
            )}
            {(g.description || g.amountPerSession !== null) && (
              <div className="goal-foot">
                <span>{g.description}</span>
                {g.amountPerSession !== null && (
                  <span>
                    {g.amountPerSession} {g.unit}
                  </span>
                )}
              </div>
            )}
          </div>
        ))}
      </div>
      <dialog ref={dialogRef} className="goal-dialog">
        <div className="box-header">
          <h2>Nytt mål</h2>
          <button
            type="button"
            className="btn-outline"
            onClick={() => dialogRef.current?.close()}
          >
            Lukk
          </button>
        </div>
        <GoalForm
          onCreate={onCreate}
          onDone={() => dialogRef.current?.close()}
        />
      </dialog>
    </>
  );
}
