import { useState } from "react";
import { useNavigate } from "react-router";
import BackButton from "../shared/components/BackButton";
import Topbar from "../shared/components/Topbar";
import { HOME_PATH } from "../shared/config/routes";
import { GoalStatus, useGoals } from "../features/goals";
import GoalWizard from "../features/goals/catalog/components/GoalWizard";
import GoalList from "../features/goals/components/GoalList";

export default function GoalsPage() {
  const navigate = useNavigate();
  const [tab, setTab] = useState<GoalStatus>("ACTIVE");
  const [wizardKey, setWizardKey] = useState(0);
  const [actionError, setActionError] = useState("");
  const {
    goals,
    loading,
    loadError,
    addGoal,
    archiveGoal,
    unarchiveGoal,
    removeGoal,
  } = useGoals(tab);

  async function run(action: () => Promise<void>) {
    setActionError("");
    try {
      await action();
    } catch (err) {
      setActionError((err as Error).message || "Something went wrong");
    }
  }

  function handleCreated() {
    setTab("ACTIVE");
    setWizardKey((k) => k + 1);
  }

  return (
    <>
      <Topbar />
      <div className="page">
        <h1>Goals</h1>

        <section className="goals-box">
          <div className="box-header">
            <h2>Create a new goal</h2>
          </div>
          <GoalWizard
            key={wizardKey}
            onCreate={addGoal}
            onDone={handleCreated}
          />
        </section>

        <section className="goals-box">
          <div className="box-header">
            <h2>My goals</h2>
            <div className="tabs">
              <button
                type="button"
                className={"tab" + (tab === "ACTIVE" ? " active" : "")}
                onClick={() => setTab("ACTIVE")}
              >
                Active
              </button>
              <button
                type="button"
                className={"tab" + (tab === "ARCHIVED" ? " active" : "")}
                onClick={() => setTab("ARCHIVED")}
              >
                Archived
              </button>
            </div>
          </div>
          {actionError && <p className="error">{actionError}</p>}
          {tab === "ACTIVE" ? (
            <GoalList
              goals={goals}
              loading={loading}
              loadError={loadError}
              emptyText="No goals yet. Create your first one above."
              onArchive={(id) => run(() => archiveGoal(id))}
            />
          ) : (
            <GoalList
              goals={goals}
              loading={loading}
              loadError={loadError}
              emptyText="No archived goals."
              onUnarchive={(id) => run(() => unarchiveGoal(id))}
              onDelete={(g) => {
                if (
                  window.confirm(
                    `Delete "${g.title}" permanently? This can't be undone.`,
                  )
                ) {
                  void run(() => removeGoal(g.id));
                }
              }}
            />
          )}
        </section>

        <BackButton onBack={() => navigate(HOME_PATH)} />
      </div>
    </>
  );
}
