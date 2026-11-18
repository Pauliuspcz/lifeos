/*The Focus part of picking a goal using our templates*/
import { useCategoryDirections } from "../../hooks/useCategoryDirections";
import type { Category, Direction } from "../../types";
import WizardProgress from "../WizardProgress";

type Props = {
  area: Category;
  onBack: () => void;
  onPick: (direction: Direction) => void;
  onCustom: () => void;
};

export default function FocusStep({ area, onBack, onPick, onCustom }: Props) {
  const { directions, loading, loadError } = useCategoryDirections(area.id);

  return (
    <div>
      <WizardProgress step={2} color={area.color} />
      <button type="button" className="btn-outline" onClick={onBack}>
        ← Back
      </button>
      <h3 style={{ color: area.color ?? undefined }}>{area.name}</h3>
      <p className="muted">What do you want to change?</p>
      {loading && <p className="muted">Loading…</p>}
      {loadError && <p className="error">{loadError}</p>}
      <div className="direction-grid">
        {directions.map((d) => (
          <button
            key={d.id}
            type="button"
            className="direction-card"
            style={{ borderLeft: `4px solid ${area.color ?? "#ccc"}` }}
            onClick={() => onPick(d)}
          >
            <strong>{d.name}</strong>
            {d.description && <span className="muted">{d.description}</span>}
          </button>
        ))}
      </div>
      <button type="button" className="btn-outline" onClick={onCustom}>
        I'll create my own
      </button>
    </div>
  );
}
