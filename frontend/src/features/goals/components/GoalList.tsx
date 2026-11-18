import { Archive, ArchiveRestore, Trash2 } from "lucide-react";
import { useMemo, useState, type CSSProperties } from "react";
import GoalIcon from "../catalog/components/GoalIcon";
import { useAreas } from "../catalog/hooks/useAreas";
import { describeGoal } from "../GoalDisplay";
import type { Goal, GoalPeriod } from "../types";

type Props = {
  goals: Goal[];
  loading: boolean;
  loadError: string;
  emptyText: string;
  onArchive?: (id: number) => void;
  onUnarchive?: (id: number) => void;
  onDelete?: (goal: Goal) => void;
};

const VISIBLE = 4;
const PERIOD_ORDER: Record<GoalPeriod, number> = {
  DAILY: 0,
  WEEKLY: 1,
  MONTHLY: 2,
  YEARLY: 3,
};
const rank = (g: Goal) => (g.period ? PERIOD_ORDER[g.period] : 4);
const byPeriodThenTitle = (a: Goal, b: Goal) =>
  rank(a) - rank(b) || a.title.localeCompare(b.title);

export default function GoalList({
  goals,
  loading,
  loadError,
  emptyText,
  onArchive,
  onUnarchive,
  onDelete,
}: Props) {
  const { areas, loading: areasLoading } = useAreas();
  const [expanded, setExpanded] = useState<Set<number>>(new Set());

  const groups = useMemo(
    () =>
      [...areas]
        .sort((a, b) => a.sortOrder - b.sortOrder)
        .map((area) => ({
          area,
          items: goals
            .filter((g) => g.categoryId === area.id)
            .sort(byPeriodThenTitle),
        }))
        .filter((g) => g.items.length > 0),
    [areas, goals],
  );

  function toggle(id: number) {
    setExpanded((prev) => {
      const next = new Set(prev);
      if (!next.delete(id)) next.add(id);
      return next;
    });
  }

  return (
    <div>
      {(loading || areasLoading) && <p className="muted">Loading goals…</p>}
      {loadError && <p className="error">{loadError}</p>}
      {!loading && !loadError && goals.length === 0 && (
        <p className="muted">{emptyText}</p>
      )}

      <div className="goal-area-grid">
        {groups.map(({ area, items }) => {
          const open = expanded.has(area.id);
          const visible = open ? items : items.slice(0, VISIBLE);
          const style = {
            "--area-color": area.color ?? "#999",
          } as CSSProperties;
          return (
            <section className="goal-area-card" key={area.id} style={style}>
              <header className="goal-area-head">
                <span className="goal-area-title">
                  <GoalIcon name={area.iconKey} size={18} />
                  <strong>{area.name}</strong>
                </span>
                <span className="muted">{items.length}</span>
              </header>

              {visible.map((g) => (
                <div className="goal-mini" key={g.id}>
                  <span className="goal-mini-icon">
                    <GoalIcon name={g.iconKey ?? area.iconKey} size={16} />
                  </span>
                  <div className="goal-mini-main">
                    <span className="goal-mini-title">{g.title}</span>
                    <span className="muted goal-mini-sub">
                      {g.archivedAt
                        ? `Archived ${new Date(g.archivedAt).toLocaleDateString("en-GB")}`
                        : describeGoal(g)}
                    </span>
                  </div>
                  {onArchive && (
                    <button
                      type="button"
                      className="icon-btn"
                      title="Archive"
                      aria-label={"Archive " + g.title}
                      onClick={() => onArchive(g.id)}
                    >
                      X
                    </button>
                  )}
                  {onUnarchive && (
                    <button
                      type="button"
                      className="icon-btn"
                      title="Restore"
                      aria-label={"Restore " + g.title}
                      onClick={() => onUnarchive(g.id)}
                    >
                      Restore
                    </button>
                  )}
                  {onDelete && (
                    <button
                      type="button"
                      className="icon-btn danger"
                      title="Delete permanently"
                      aria-label={"Delete " + g.title}
                      onClick={() => onDelete(g)}
                    >
                      Delete
                    </button>
                  )}
                </div>
              ))}

              {items.length > VISIBLE && (
                <button
                  type="button"
                  className="show-more"
                  onClick={() => toggle(area.id)}
                >
                  {open ? "Show less" : `+${items.length - VISIBLE} more`}
                </button>
              )}
            </section>
          );
        })}
      </div>
    </div>
  );
}
