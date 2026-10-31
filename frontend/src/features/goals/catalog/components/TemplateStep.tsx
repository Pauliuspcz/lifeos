import { useDirectionTemplates } from "../hooks/useDirectionTemplates";
import { DIFFICULTY_LABEL, describeTemplate } from "../templateDisplay";
import type { Direction, Template } from "../types";

type Props = {
  direction: Direction;
  onBack: () => void;
  onPick: (template: Template) => void;
};

export default function TemplateStep({ direction, onBack, onPick }: Props) {
  const { templates, categories, loading, loadError } = useDirectionTemplates(
    direction.id,
  );

  return (
    <div>
      <button type="button" className="btn-outline" onClick={onBack}>
        ← Go back
      </button>
      <h3>{direction.name}</h3>
      {loading && <p className="muted">Loading…</p>}
      {loadError && <p className="error">{loadError}</p>}

      {categories.map((c) => {
        const inCategory = templates.filter((t) => t.categoryId === c.id);
        if (inCategory.length === 0) return null;
        return (
          <section key={c.id}>
            <h4 style={{ color: c.color ?? undefined }}>{c.name}</h4>
            <div className="direction-grid">
              {inCategory.map((t) => (
                <button
                  key={t.id}
                  type="button"
                  className="direction-card"
                  style={{
                    borderLeft: `4px solid ${t.color ?? c.color ?? "#ccc"}`,
                  }}
                  onClick={() => onPick(t)}
                >
                  <strong>{t.title}</strong>
                  <span className="muted">{describeTemplate(t)}</span>
                  {t.difficulty && (
                    <span className="muted">
                      {" - " + DIFFICULTY_LABEL[t.difficulty]}
                    </span>
                  )}
                </button>
              ))}
            </div>
          </section>
        );
      })}
    </div>
  );
}
