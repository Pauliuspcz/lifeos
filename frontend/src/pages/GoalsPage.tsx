import { useMemo, useRef, useState } from "react";
import { Check } from "lucide-react";
import Topbar from "../shared/components/Topbar";
import { GoalStatus, useGoals, type Category } from "../features/goals";
import GoalWizard from "../features/goals/catalog/components/GoalWizard";
import GoalList from "../features/goals/components/GoalList";
import {
  countInPeriod,
  listProgressForGoal,
  useAmountSummaries,
  useProgress,
} from "../features/progress";
import {
  RelationHints,
  titleLookup,
  toGoalRef,
  useRelationHints,
} from "../features/relations";

export default function GoalsPage() {
  const [tab, setTab] = useState<GoalStatus>("ACTIVE");
  const [wizardKey, setWizardKey] = useState(0);
  const [wizardArea, setWizardArea] = useState<Category | null>(null);
  const dialogRef = useRef<HTMLDialogElement>(null);
  const [actionError, setActionError] = useState("");
  const [notice, setNotice] = useState("");
  const {
    goals,
    loading,
    loadError,
    addGoal,
    archiveGoal,
    unarchiveGoal,
    removeGoal,
  } = useGoals(tab);

  const { entries } = useProgress();
  const activeGoals = useMemo(
    () => (tab === "ACTIVE" ? goals : []),
    [tab, goals],
  );
  const summaries = useAmountSummaries(activeGoals, entries);

  const hints = useRelationHints(
    goals.map(toGoalRef),
    null,
    tab === "ACTIVE" && goals.length > 0,
  );

  async function run(action: () => Promise<void>) {
    setActionError("");
    try {
      await action();
    } catch (err) {
      setActionError((err as Error).message || "Something went wrong");
    }
  }

  function openWizard(area: Category | null) {
    setWizardArea(area);
    dialogRef.current?.showModal();
  }

  function handleCreated() {
    dialogRef.current?.close();
    setTab("ACTIVE");
    setNotice("Goal created");
    window.setTimeout(() => setNotice(""), 3000);
  }

  return (
    <>
      <Topbar />
      <div className="goals-page">
        <header className="goals-page-head">
          <h1>Goals</h1>
          <button
            type="button"
            className="btn-dark"
            onClick={() => openWizard(null)}
          >
            + New goal
          </button>
        </header>

        <nav className="goals-tabs">
          <button
            type="button"
            className={tab === "ACTIVE" ? "active" : ""}
            onClick={() => setTab("ACTIVE")}
          >
            Active
            {tab === "ACTIVE" && !loading && (
              <span className="tab-count">{goals.length}</span>
            )}
          </button>
          <button
            type="button"
            className={tab === "ARCHIVED" ? "active" : ""}
            onClick={() => setTab("ARCHIVED")}
          >
            Archived
          </button>
        </nav>

        {tab === "ACTIVE" && (
          <RelationHints hints={hints} titleOf={titleLookup(goals)} max={3} />
        )}
        {notice && (
          <p className="notice" role="status">
            {notice} <Check size={18} />
          </p>
        )}
        {actionError && <p className="error">{actionError}</p>}

        {tab === "ACTIVE" ? (
          <GoalList
            goals={goals}
            loading={loading}
            loadError={loadError}
            emptyText="No goals yet."
            showEmptyAreas
            countFor={(g) => countInPeriod(entries, g)}
            totalFor={(g) => summaries[g.id]?.total}
            onAdd={openWizard}
            onArchive={(id) => run(() => archiveGoal(id))}
          />
        ) : (
          <GoalList
            goals={goals}
            loading={loading}
            loadError={loadError}
            emptyText="No archived goals."
            onUnarchive={(id) => run(() => unarchiveGoal(id))}
            onDelete={(g) =>
              void run(async () => {
                const checkins = await listProgressForGoal(g.id);
                if (checkins.length > 0) {
                  setActionError(
                    `"${g.title}" has check-ins, so it can't be deleted. It stays archived to keep your history.`,
                  );
                  return;
                }
                if (
                  window.confirm(
                    `Delete "${g.title}" permanently? This can't be undone.`,
                  )
                ) {
                  await removeGoal(g.id);
                }
              })
            }
          />
        )}
      </div>

      <dialog
        ref={dialogRef}
        className="goal-dialog"
        onClose={() => setWizardKey((k) => k + 1)}
      >
        <GoalWizard
          key={`${wizardKey}-${wizardArea?.id ?? "all"}`}
          variant="dialog"
          title="New goal"
          initialArea={wizardArea}
          activeGoals={tab === "ACTIVE" ? goals : undefined}
          onClose={() => dialogRef.current?.close()}
          onCreate={addGoal}
          onDone={handleCreated}
        />
      </dialog>
    </>
  );
}
