import { CreateGoalRequest, GoalPeriod, PERIODS } from "../types/goal";
import React, { useState } from "react";

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
  async function handleSubmit(e: React.SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!form.title.trim()) return setError("Title is required");
    if (form.timesPerPeriod < 1)
      return setError("Times per period must be at least 1");
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
            placeholder="Title"
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
                {p}
              </option>
            ))}
          </select>
        </div>
        <div>
          <input
            type="number"
            min={1}
            value={form.timesPerPeriod}
            onChange={(e) =>
              setForm({ ...form, timesPerPeriod: Number(e.target.value) })
            }
          />
        </div>
        <button>Create</button>
      </form>
      {error && <p style={{ color: "red" }}> {error}</p>}
    </>
  );
}
