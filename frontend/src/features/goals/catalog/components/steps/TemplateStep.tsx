/*The last step in creating a goal */
import type { CSSProperties } from "react";
import { useDirectionTemplates } from "../../hooks/useDirectionTemplates";
import { DIFFICULTY_LABEL, describeTemplate } from "../../templateDisplay";
import type { Category, Direction, Template } from "../../types";
import GoalIcon from "../GoalIcon";
import WizardProgress from "../WizardProgress";
import TemplateBadges from "../TemplateBadges";

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
  const color = area.color ?? "#ccc";

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
            style={
              {
                borderLeft: `4px solid ${color}`,
                "--area-color": color,
              } as CSSProperties
            }
            onClick={() => onPick(t)}
          >
            <span className="area-head">
              <strong>{t.title}</strong>
              <span className="area-icon">
                <GoalIcon name={t.iconKey} size={20} />
              </span>
            </span>
            <span className="muted">{describeTemplate(t)}</span>
            {t.difficulty && (
              <span className="muted">{DIFFICULTY_LABEL[t.difficulty]}</span>
            )}
            {t.outdoor && <span className="muted">Outdoor</span>}
            <TemplateBadges template={t} />
          </button>
        ))}
      </div>
      <button type="button" className="btn-outline" onClick={onCustom}>
        I'll create my own
      </button>
    </div>
  );
}
