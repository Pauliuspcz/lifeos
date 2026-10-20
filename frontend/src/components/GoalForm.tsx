import {
  CreateGoalRequest,
  GoalPeriod,
  PERIODS,
  PERIOD_LABEL,
} from "../features/goals/types";
import React, { useState, type SubmitEvent } from "react";

type Props = {
  onCreate: (req: CreateGoalRequest) => Promise<void>;
  onDone?: () => void;
};

export default function GoalForm({ onCreate, onDone }: Props) {
  const [form, setForm] = useState<CreateGoalRequest>({
    title: "",
    description: null,
    period: "WEEKLY",
    timesPerPeriod: 1,
    amountPerSession: null,
  });
  const [error, setError] = useState("");
  async function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!form.title.trim()) return setError("Tittel må fylles ut");
    if (form.timesPerPeriod < 1)
      return setError("Antall ganger må være minst 1");
    try {
      await onCreate(form);
      setError("");
      setForm({ ...form, title: "" });
      onDone?.();
    } catch (err) {
      setError((err as Error).message);
    }
  }
  return (
    <>
      <form onSubmit={handleSubmit}>
        <div>
          <input
            placeholder="Tittel"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
          />
        </div>
        <div>
          <select
            value={form.period}
            onChange={(e) =>
              setForm({ ...form, period: e.target.value as GoalPeriod })
            }
          >
            {PERIODS.map((p) => (
              <option key={p} value={p}>
                per {PERIOD_LABEL[p]}
              </option>
            ))}
          </select>
        </div>
        <div>
          <input
            type="number"
            placeholder="Ganger per periode"
            min={1}
            value={form.timesPerPeriod}
            onChange={(e) =>
              setForm({ ...form, timesPerPeriod: Number(e.target.value) })
            }
          />
        </div>
        <button type="submit">Opprett</button>
      </form>
      {error && <p className="error">{error}</p>}
    </>
  );
}
