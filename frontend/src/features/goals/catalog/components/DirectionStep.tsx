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
      <h3>Hva vil du oppnå?</h3>
      {loading && <p className="muted">Laster…</p>}
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
            {d.description && <span className="muted">{d.description}</span>}
          </button>
        ))}
      </div>
      <button type="button" className="btn-outline" onClick={onCustom}>
        Lag mitt eget mål
      </button>
    </div>
  );
}
