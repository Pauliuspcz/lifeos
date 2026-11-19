import { Check } from "lucide-react";
import { useState, type FormEvent } from "react";
import type { Goal } from "../../goals";
import type { CreateProgressRequest, GoalProgressSummary } from "../types";
import { formatAmount } from "../utils/pace";

type Props = {
  goals: Goal[];
  summaries: Record<number, GoalProgressSummary>;
  onLog: (req: CreateProgressRequest) => Promise<void>;
  loadError: string;
};

export default function LogProgressCard({
  goals,
  summaries,
  onLog,
  loadError,
}: Props) {
  const amountGoals = goals.filter((g) => g.trackingType === "AMOUNT");
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [value, setValue] = useState("");
  const [error, setError] = useState("");
  const selected = amountGoals.find((g) => g.id === selectedId);

  async function submit(e: FormEvent) {
    e.preventDefault();
    if (!selected) return;
    const amount = Math.round(Number(value.replace(",", ".")) * 100) / 100;
    if (!Number.isFinite(amount) || amount <= 0) {
      setError("Enter an amount above 0");
      return;
    }
    try {
      await onLog({
        goalId: selected.id,
        date: new Date().toLocaleDateString("sv-SE"),
        amount,
      });
      setValue("");
      setError("");
    } catch (err) {
      setError((err as Error).message);
    }
  }

  return (
    <div className="amount-box">
      <h2>Log progress</h2>
      <p className="muted">Goals with a running total</p>
      {loadError && <p className="muted">Could not load active goals</p>}
      {!loadError && amountGoals.length === 0 && (
        <p className="muted">No amount goals yet</p>
      )}
      {amountGoals.map((g) => {
        const s = summaries[g.id];
        const isSelected = g.id === selected?.id;
        const unit = g.unit ? ` ${g.unit}` : "";
        return (
          <button
            type="button"
            key={g.id}
            className={"habit-row" + (isSelected ? " done" : "")}
            aria-pressed={isSelected}
            onClick={() => setSelectedId(isSelected ? null : g.id)}
          >
            <span className="check">
              {isSelected && <Check size={12} strokeWidth={3} />}
            </span>
            <span className="habit-text">
              <span className="habit-title">{g.title}</span>
            </span>
            <span className="muted">
              {s
                ? formatAmount(s.total) +
                  (s.target !== null ? ` / ${formatAmount(s.target)}` : "") +
                  unit
                : "…"}
            </span>
          </button>
        );
      })}
      {amountGoals.length > 0 && (
        <form onSubmit={submit}>
          <p className="label">Add progress</p>
          <div className="journal">
            <label className="amount-field">
              <input
                type="number"
                inputMode="decimal"
                step="any"
                min="0"
                placeholder={selected ? "How much?" : "Pick a goal first"}
                aria-label={
                  selected ? `Amount for ${selected.title}` : "Amount"
                }
                disabled={!selected}
                value={value}
                onChange={(e) => setValue(e.target.value)}
              />
              {selected?.unit && (
                <span className="amount-unit">{selected.unit}</span>
              )}
            </label>
            <button type="submit" className="btn-dark" disabled={!selected}>
              Send
            </button>
          </div>
          {error && <p className="error">{error}</p>}
        </form>
      )}
    </div>
  );
}
