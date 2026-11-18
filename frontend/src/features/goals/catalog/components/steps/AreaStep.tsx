/*The Area part of picking a goal using our templates*/
import type { CSSProperties } from "react";
import { useAreas } from "../../hooks/useAreas";
import type { Category } from "../../types";
import GoalIcon from "../GoalIcon";
import WizardProgress from "../WizardProgress";

type Props = { onPick: (area: Category) => void };

function countText(n: number): string {
  if (n === 0) return "No active goals yet";
  return n === 1 ? "1 active goal" : `${n} active goals`;
}

export default function AreaStep({ onPick }: Props) {
  const { areas, counts, loading, loadError } = useAreas();

  return (
    <div>
      <WizardProgress step={1} />
      <h3>What part of you will change?</h3>
      {loading && <p className="muted">Loading…</p>}
      {loadError && <p className="error">{loadError}</p>}
      <div className="direction-grid area-grid">
        {areas.map((a) => (
          <button
            key={a.id}
            type="button"
            className="direction-card area-card"
            style={{ "--area-color": a.color ?? "#ccc" } as CSSProperties}
            onClick={() => onPick(a)}
          >
            <span className="area-head">
              <span className="area-icon">
                <GoalIcon name={a.iconKey} size={20} />
              </span>
              <strong>{a.name}</strong>
            </span>
            {a.description && <span className="muted">{a.description}</span>}
            <span className="muted">{countText(counts[a.id] ?? 0)}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
