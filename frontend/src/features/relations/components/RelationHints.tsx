import { useState } from "react";
import type { RelationHint, RelationType } from "../types";
import { humanize } from "../utils";

type Props = {
  hints: RelationHint[];
  titleOf?: (code: string) => string;
  max?: number;
};

const ORDER: Record<RelationType, number> = {
  OVERLOAD: 0,
  TENSION: 1,
  REDUNDANT: 2,
  SUPPORTS: 3,
};

function headline(h: RelationHint, title: (code: string) => string): string {
  const names = h.involved.map(title).join(" + ");
  switch (h.type) {
    case "SUPPORTS":
      return h.suggestion
        ? `Consider adding: ${title(h.suggestion)}`
        : `${names} work well together`;
    case "TENSION":
      return `Heads up: ${names}`;
    case "REDUNDANT":
      return `Overlap: ${names}`;
    case "OVERLOAD":
      return "A lot of daily goals";
  }
}

export default function RelationHints({
  hints,
  titleOf = humanize,
  max = 4,
}: Props) {
  const [expanded, setExpanded] = useState(false);
  if (hints.length === 0) return null;

  const sorted = [...hints].sort((a, b) => ORDER[a.type] - ORDER[b.type]);
  const shown = expanded ? sorted : sorted.slice(0, max);
  const rest = sorted.length - shown.length;

  return (
    <div>
      <ul className="relation-list" aria-label="Goal suggestions">
        {shown.map((h) => (
          <li
            key={`${h.type}|${h.suggestion}|${h.reason}`}
            className={`relation relation-${h.type.toLowerCase()}`}
          >
            <strong>{headline(h, titleOf)}</strong>
            <span>{h.reason}</span>
          </li>
        ))}
      </ul>
      {sorted.length > max && (
        <button
          type="button"
          className="show-more"
          onClick={() => setExpanded((e) => !e)}
        >
          {expanded ? "Show less" : `+${rest} more`}
        </button>
      )}
    </div>
  );
}
