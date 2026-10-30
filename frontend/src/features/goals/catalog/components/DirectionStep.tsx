import { useDirections } from "../hooks/useDirections";
import type { Direction } from "../types";

type Props = {
  onPick: (direction: Direction) => void;
  onCustom: () => void;
};

export default function DirectionStep({ onPick, onCustom }: Props) {
  const { directions, loading, loadError } = useDirections();

  return (
    <div>
      <h3>What is the end goal?</h3>
      {loading && <p className="muted">Loading your next BIG step…</p>}
      {loadError && <p className="error">{loadError}</p>}
      <div className="direction-grid">
        {directions.map((d) => (
          <button
            key={d.id}
            type="button"
            className="direction-card"
            style={{ borderLeft: `4px solid ${d.color ?? "#ccc"}` }}
            onClick={() => onPick(d)}
          >
            <strong>{d.name}</strong>
            {d.description && (
              <span className="muted">{" - " + d.description}</span>
            )}
          </button>
        ))}
      </div>
      <button type="button" className="btn-outline" onClick={onCustom}>
        I'll create my own
      </button>
    </div>
  );
}
