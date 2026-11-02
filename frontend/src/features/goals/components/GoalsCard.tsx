import type { CreateGoalRequest, Goal } from "../types";
import { useRef, useState } from "react";
import { describeGoal } from "../GoalDisplay";
import GoalWizard from "../catalog/components/GoalWizard";

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
  const [wizardKey, setWizardKey] = useState(0);
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
          <h2>Goals</h2>
          <button
            type="button"
            className="btn-outline"
            onClick={() => dialogRef.current?.showModal()}
          >
            + New goal
          </button>
        </div>
        {loading && <p className="muted">Loading goals…</p>}
        {loadError && <p className="error">{loadError}</p>}
        {!loading && !loadError && goals.length === 0 && (
          <p className="muted">No goals created yet.</p>
        )}
        {goals.map((g) => (
          <div className="goal-row" key={g.id}>
            <div className="goal-top">
              <span>{g.title}</span>
              <span className="muted">{describeGoal(g)}</span>
              <button
                type="button"
                className="goal-archive"
                aria-label={"Archive " + g.title}
                onClick={() => handleArchive(g.id)}
              >
                ×
              </button>
            </div>
            {countFor && g.trackingType === "COUNT" && g.timesPerPeriod && (
              <div className="progress">
                <div
                  className="progress-fill"
                  style={{
                    width: `${Math.min(100, (countFor(g) / g.timesPerPeriod) * 100)}%`,
                  }}
                />
              </div>
            )}
            {(g.description ||
              (g.trackingType === "COUNT" && g.amountPerSession !== null)) && (
              <div className="goal-foot">
                <span>{g.description}</span>
                {g.trackingType === "COUNT" && g.amountPerSession !== null && (
                  <span>
                    {g.amountPerSession} {g.unit}
                  </span>
                )}
              </div>
            )}
          </div>
        ))}
      </div>
      <dialog
        ref={dialogRef}
        className="goal-dialog"
        onClose={() => setWizardKey((k) => k + 1)}
      >
        <div className="box-header">
          <h2></h2>
          <button
            type="button"
            className="btn-outline"
            onClick={() => dialogRef.current?.close()}
          >
            Close
          </button>
        </div>
        <GoalWizard
          key={wizardKey}
          onCreate={onCreate}
          onDone={() => dialogRef.current?.close()}
        />
      </dialog>
    </>
  );
}
