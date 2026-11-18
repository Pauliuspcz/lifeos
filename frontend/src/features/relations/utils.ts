import type { Goal } from "../goals";
import type { GoalRef } from "./types";

export const toGoalRef = (g: Goal): GoalRef => ({
  templateCode: g.templateCode ?? null,
  directionCode: g.directionCode ?? null,
  period: g.period,
});

export function humanize(code: string): string {
  const text = code.toLowerCase().replace(/_/g, " ");
  return text.charAt(0).toUpperCase() + text.slice(1);
}

export function titleLookup(goals: Goal[]): (code: string) => string {
  const titles = new Map<string, string>();
  for (const g of goals) {
    if (g.templateCode) titles.set(g.templateCode, g.title);
  }
  return (code) => titles.get(code) ?? humanize(code);
}
