import { useEffect, useMemo, useState, type CSSProperties } from "react";
import { Archive, ArchiveRestore, Check, Ellipsis, Trash2 } from "lucide-react";
import { useAreas } from "../catalog/hooks/useAreas";
import type { Category } from "../catalog/types";
import { describeGoal } from "../GoalDisplay";
import { periodOf, timesOf, type Goal, type GoalPeriod } from "../types";

type Props = {
  goals: Goal[];
  loading: boolean;
  loadError: string;
  emptyText: string;
  showEmptyAreas?: boolean;
  countFor?: (goal: Goal) => number;
  totalFor?: (goal: Goal) => number | undefined;
  onAdd?: (area: Category) => void;
  onArchive?: (id: number) => void;
  onUnarchive?: (id: number) => void;
  onDelete?: (goal: Goal) => void;
};

const PERIOD_ORDER: Record<GoalPeriod, number> = {
  DAILY: 0,
  WEEKLY: 1,
  MONTHLY: 2,
  YEARLY: 3,
};
const PERIOD_NOW: Record<GoalPeriod, string> = {
  DAILY: "today",
  WEEKLY: "this week",
  MONTHLY: "this month",
  YEARLY: "this year",
};
const rank = (g: Goal) => (g.period ? PERIOD_ORDER[g.period] : 4);
const byPeriodThenTitle = (a: Goal, b: Goal) =>
  rank(a) - rank(b) || a.title.localeCompare(b.title);

type GoalProgress = { text: string; pct: number; done: boolean };

function progressOf(
  g: Goal,
  countFor?: (goal: Goal) => number,
  totalFor?: (goal: Goal) => number | undefined,
): GoalProgress | null {
  if (g.trackingType === "CHECK") {
    if (!countFor) return null;
    const target = timesOf(g);
    const count = countFor(g);
    const done = count >= target;
    const pct = count / target;
    if (periodOf(g) === "DAILY") {
      return { text: done ? "Done today" : "Not done today", pct, done };
    }
    return {
      text: `${count} of ${target} ${PERIOD_NOW[periodOf(g)]}`,
      pct,
      done,
    };
  }
  const total = totalFor?.(g);
  if (!g.targetAmount || total === undefined) return null;
  const when = g.period ? ` ${PERIOD_NOW[g.period]}` : "";
  return {
    text: `${total} / ${g.targetAmount} ${g.unit ?? ""}${when}`,
    pct: total / g.targetAmount,
    done: total >= g.targetAmount,
  };
}

const archivedLabel = (iso: string) =>
  "Archived " +
  new Date(iso).toLocaleDateString("en-GB", { day: "numeric", month: "short" });

export default function GoalList({
  goals,
  loading,
  loadError,
  emptyText,
  showEmptyAreas = false,
  countFor,
  totalFor,
  onAdd,
  onArchive,
  onUnarchive,
  onDelete,
}: Props) {
  const { areas, loading: areasLoading } = useAreas();
  const [menuFor, setMenuFor] = useState<number | null>(null);

  useEffect(() => {
    if (menuFor === null) return;
    const close = () => setMenuFor(null);
    const onKey = (e: KeyboardEvent) => e.key === "Escape" && close();
    document.addEventListener("click", close);
    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("click", close);
      document.removeEventListener("keydown", onKey);
    };
  }, [menuFor]);

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
        .filter((g) => showEmptyAreas || g.items.length > 0),
    [areas, goals, showEmptyAreas],
  );

  function closeMenuAnd(action: () => void) {
    setMenuFor(null);
    action();
  }

  return (
    <div>
      {(loading || areasLoading) && <p className="muted">Loading goals…</p>}
      {loadError && <p className="error">{loadError}</p>}
      {!loading && !loadError && !showEmptyAreas && goals.length === 0 && (
        <p className="muted">{emptyText}</p>
      )}

      <div className="area-list">
        {groups.map(({ area, items }) => (
          <section
            className="area-card"
            key={area.id}
            style={{ "--area-color": area.color ?? "#999" } as CSSProperties}
          >
            <header className="area-card-head">
              <span className="area-dot" />
              <strong>{area.name}</strong>
              {area.description && (
                <span className="muted area-desc">{area.description}</span>
              )}
              {onAdd && (
                <button
                  type="button"
                  className="btn-outline area-add"
                  onClick={() => onAdd(area)}
                >
                  + Add
                </button>
              )}
            </header>

            {items.length === 0 && (
              <p className="muted area-empty">No goals yet</p>
            )}

            {items.map((g) => {
              const p = g.archivedAt ? null : progressOf(g, countFor, totalFor);
              return (
                <div className="goal-line" key={g.id}>
                  <div className="goal-line-top">
                    <span className="goal-line-title">{g.title}</span>
                    <span
                      className={"goal-line-meta" + (p?.done ? " done" : "")}
                    >
                      {p?.done && <Check size={14} />}
                      {g.archivedAt
                        ? archivedLabel(g.archivedAt)
                        : (p?.text ?? describeGoal(g))}
                    </span>
                    <div className="row-menu">
                      <button
                        type="button"
                        className="row-menu-btn"
                        aria-label={"More actions for " + g.title}
                        aria-expanded={menuFor === g.id}
                        onClick={(e) => {
                          e.stopPropagation();
                          setMenuFor(menuFor === g.id ? null : g.id);
                        }}
                      >
                        <Ellipsis size={16} />
                      </button>
                      {menuFor === g.id && (
                        <div className="row-menu-list" role="menu">
                          {onArchive && (
                            <button
                              type="button"
                              role="menuitem"
                              onClick={() =>
                                closeMenuAnd(() => onArchive(g.id))
                              }
                            >
                              <Archive size={16} /> Archive
                            </button>
                          )}
                          {onUnarchive && (
                            <button
                              type="button"
                              role="menuitem"
                              onClick={() =>
                                closeMenuAnd(() => onUnarchive(g.id))
                              }
                            >
                              <ArchiveRestore size={16} /> Restore
                            </button>
                          )}
                          {onDelete && (
                            <button
                              type="button"
                              role="menuitem"
                              className="danger"
                              onClick={() => closeMenuAnd(() => onDelete(g))}
                            >
                              <Trash2 size={16} /> Delete
                            </button>
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                  {p && (
                    <div className="progress">
                      <div
                        className="progress-fill"
                        style={{
                          width: `${Math.min(100, p.pct * 100)}%`,
                          background: "var(--area-color)",
                        }}
                      />
                    </div>
                  )}
                </div>
              );
            })}
          </section>
        ))}
      </div>
    </div>
  );
}
