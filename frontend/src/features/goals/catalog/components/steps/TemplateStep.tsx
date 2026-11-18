/*The last step in creating a goal */
import { useDirectionTemplates } from "../../hooks/useDirectionTemplates";
import { DIFFICULTY_LABEL, describeTemplate } from "../../templateDisplay";
import type { Category, Direction, Template } from "../../types";
import WizardProgress from "../WizardProgress";

type Props = {
  area: Category;
  direction: Direction;
  onPick: (template: Template) => void;
  onCustom: () => void;
};

export default function TemplateStep({
  area,
  direction,
  onPick,
  onCustom,
}: Props) {
  const { templates, loading, loadError } = useDirectionTemplates(direction.id);

  return (
    <div>
      <WizardProgress step={3} color={area.color} />
      <h3 style={{ color: area.color ?? undefined }}>{direction.name}</h3>
      <p className="muted">Pick a goal to work on over time</p>
      {loading && <p className="muted">Loading…</p>}
      {loadError && <p className="error">{loadError}</p>}
      <div className="direction-grid">
        {templates.map((t) => (
          <button
            key={t.id}
            type="button"
            className="direction-card"
            style={{ borderLeft: `4px solid ${area.color ?? "#ccc"}` }}
            onClick={() => onPick(t)}
          >
            <strong>{t.title}</strong>
            <span className="muted">{describeTemplate(t)}</span>
            {t.difficulty && (
              <span className="muted">{DIFFICULTY_LABEL[t.difficulty]}</span>
            )}
            {t.outdoor && <span className="muted">Outdoor</span>}
          </button>
        ))}
      </div>
      <button type="button" className="btn-outline" onClick={onCustom}>
        I'll create my own
      </button>
    </div>
  );
}
