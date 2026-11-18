import { Flame, House, Sun } from "lucide-react";
import { DIFFICULTY_LABEL } from "../templateDisplay";
import type { Template } from "../types";

const FLAMES: Record<NonNullable<Template["difficulty"]>, number> = {
  EASY: 1,
  MEDIUM: 2,
  HARD: 3,
};

export default function TemplateBadges({ template }: { template: Template }) {
  const { difficulty, outdoor } = template;
  return (
    <span className="badge-row">
      {difficulty && (
        <span
          className="badge"
          title={DIFFICULTY_LABEL[difficulty]}
          aria-label={DIFFICULTY_LABEL[difficulty]}
        >
          {Array.from({ length: FLAMES[difficulty] }, (_, i) => (
            <Flame key={i} size={16} />
          ))}
        </span>
      )}
      <span className="badge">
        {outdoor ? <Sun size={16} /> : <House size={16} />}
        {outdoor ? "Outdoor" : "Indoor"}
      </span>
    </span>
  );
}
