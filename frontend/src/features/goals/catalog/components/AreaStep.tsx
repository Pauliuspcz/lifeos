import { useAreas } from "../hooks/useAreas";
import type { Category } from "../types";
import WizardProgress from "./WizardProgress";

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
      <h3>What do you want to get better at?</h3>
      {loading && <p className="muted">Loading…</p>}
      {loadError && <p className="error">{loadError}</p>}
      <div className="direction-grid area-grid">
        {areas.map((a) => (
          <button
            key={a.id}
            type="button"
            className="direction-card area-card"
            style={{ borderLeft: `4px solid ${a.color ?? "#ccc"}` }}
            onClick={() => onPick(a)}
          >
            <strong>{a.name}</strong>
            {a.description && <span className="muted">{a.description}</span>}
            <span className="muted">{countText(counts[a.id] ?? 0)}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
