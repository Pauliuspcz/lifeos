import type { CreateGoalRequest, Goal } from "../types";
import { useRef, useState } from "react";
import { describeGoal } from "../GoalDisplay";
import GoalWizard from "../catalog/components/GoalWizard";
import { Check } from "lucide-react";

type Props = {
  goals: Goal[];
  loading: boolean;
  loadError: string;
  onCreate: (req: CreateGoalRequest) => Promise<void>;
  onArchive: (goalId: number) => Promise<void>;
  countFor?: (goal: Goal) => number;
  totalFor?: (goal: Goal) => number | undefined;
};

export default function GoalsCard({
  goals,
  loading,
  loadError,
  onCreate,
  onArchive,
  countFor,
  totalFor,
}: Props) {
  const [wizardKey, setWizardKey] = useState(0);
  const dialogRef = useRef<HTMLDialogElement>(null);
  const [notice, setNotice] = useState("");

  async function handleArchive(goalId: number) {
    try {
      await onArchive(goalId);
    } catch (err) {
      alert((err as Error).message);
    }
  }

  function handleCreated() {
    dialogRef.current?.close();
    setNotice("Goal created");
    window.setTimeout(() => setNotice(""), 3000);
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
        {notice && (
          <p className="notice" role="status">
            {notice} <Check size={18} />
          </p>
        )}
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
            {countFor && g.trackingType === "CHECK" && g.timesPerPeriod && (
              <div className="progress">
                <div
                  className="progress-fill"
                  style={{
                    width: `${Math.min(100, (countFor(g) / g.timesPerPeriod) * 100)}%`,
                    background: g.color ?? undefined,
                  }}
                />
              </div>
            )}
            {totalFor && g.trackingType === "AMOUNT" && g.targetAmount && (
              <div className="progress">
                <div
                  className="progress-fill"
                  style={{
                    width: `${Math.min(100, ((totalFor(g) ?? 0) / g.targetAmount) * 100)}%`,
                    background: g.color ?? undefined,
                  }}
                />
              </div>
            )}
            {g.description && (
              <div className="goal-foot">
                <span>{g.description}</span>
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
        <GoalWizard
          key={wizardKey}
          variant="dialog"
          title="New goal"
          activeGoals={goals}
          onClose={() => dialogRef.current?.close()}
          onCreate={onCreate}
          onDone={handleCreated}
        />
      </dialog>
    </>
  );
}
