import { useGoals } from "../hooks/useGoals";
import React, { useState } from "react";
import { CreateGoalRequest, GoalPeriod, PERIODS } from "../types/goal";
import BackButton from "../components/BackButton";
import { useNavigate } from "react-router";

export default function GoalAdd() {
  const navigate = useNavigate();
  const { goals, loading, loadError, addGoal } = useGoals();
  const [form, setForm] = useState<CreateGoalRequest>({
    title: "",
    description: null,
    period: "WEEKLY",
    timesPerPeriod: 1,
    amountPerSession: null,
  });
  const [error, setError] = useState("");

  function handleBack() {
    navigate("/");
  }

  async function handleSubmit(e: React.SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!form.title.trim()) return setError("Title is required");
    if (form.timesPerPeriod < 1)
      return setError("Times per period must be at least 1");
    try {
      await addGoal(form);
      setError("");
      setForm({ ...form, title: "" });
    } catch (err) {
      setError((err as Error).message);
    }
  }

  return (
    <div className="page">
      <h1>Tasks</h1>
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
      {loading && <p>Loading...</p>}
      {loadError && <p style={{ color: "red" }}>{loadError}</p>}
      <ul>
        {goals.map((g) => (
          <li key={g.goalId}>
            {g.title} - {g.timesPerPeriod}x {g.period} ({g.status})
          </li>
        ))}
      </ul>
      <BackButton onBack={handleBack} />
    </div>
  );
}
