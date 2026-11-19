/*The Focus part of picking a goal using our templates*/
import type { CSSProperties } from "react";
import { useCategoryDirections } from "../../hooks/useCategoryDirections";
import type { Category, Direction } from "../../types";
import GoalIcon from "../GoalIcon";
import WizardProgress from "../WizardProgress";

type Props = {
  area: Category;
  onPick: (direction: Direction) => void;
  onCustom: () => void;
};

export default function FocusStep({ area, onPick, onCustom }: Props) {
  const { directions, loading, loadError } = useCategoryDirections(area.id);
  const color = area.color ?? "#ccc";

  return (
    <div>
      <WizardProgress step={2} color={area.color} />
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
            style={
              {
                borderLeft: `4px solid ${color}`,
                "--area-color": color,
              } as CSSProperties
            }
            onClick={() => onPick(d)}
          >
            <span className="area-head">
              <strong>{d.name}</strong>
              <span className="area-icon">
                <GoalIcon name={d.iconKey} size={20} />
              </span>
            </span>
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
