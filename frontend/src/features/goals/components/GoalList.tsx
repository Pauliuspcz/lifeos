import { describeGoal } from "../GoalDisplay";
import type { Goal } from "../types";

type Props = {
  goals: Goal[];
  loading: boolean;
  loadError: string;
  emptyText: string;
  onArchive?: (id: number) => void;
  onUnarchive?: (id: number) => void;
  onDelete?: (goal: Goal) => void;
};

export default function GoalList({
  goals,
  loading,
  loadError,
  emptyText,
  onArchive,
  onUnarchive,
  onDelete,
}: Props) {
  return (
    <div>
      {loading && <p className="muted">Loading goals…</p>}
      {loadError && <p className="error">{loadError}</p>}
      {!loading && !loadError && goals.length === 0 && (
        <p className="muted">{emptyText}</p>
      )}
      {goals.map((g) => (
        <div
          className="goal-row"
          key={g.id}
          style={{
            width: "100px",
            borderLeft: `4px solid ${g.color ?? "#ccc"}`,
          }}
        >
          <div className="goal-top">
            <strong>{g.title}</strong>
            <span className="muted">{describeGoal(g)}</span>
          </div>
          {g.description && <p className="muted">{g.description}</p>}
          {g.motivation && <p className="muted">"{g.motivation}"</p>}
          {g.archivedAt && (
            <p className="muted">
              Archived {new Date(g.archivedAt).toLocaleDateString("en-GB")}
            </p>
          )}
          <div>
            {onArchive && (
              <button
                type="button"
                className="btn-outline"
                onClick={() => onArchive(g.id)}
              >
                Archive
              </button>
            )}
            {onUnarchive && (
              <button
                type="button"
                className="btn-outline"
                onClick={() => onUnarchive(g.id)}
              >
                Restore
              </button>
            )}
            {onDelete && (
              <button
                type="button"
                className="btn-outline btn-danger"
                onClick={() => onDelete(g)}
              >
                Delete permanently
              </button>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}
