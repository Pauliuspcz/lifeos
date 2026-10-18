import { useGoals } from "../hooks/useGoals";
import React from "react";
import BackButton from "../components/BackButton";
import { useNavigate } from "react-router";
import GoalForm from "../components/GoalForm";

export default function GoalAdd() {
  const navigate = useNavigate();
  const { goals, loading, loadError, addGoal } = useGoals();

  function handleBack() {
    navigate("/");
  }
  return (
    <div className="page">
      <h1>Tasks</h1>
      <GoalForm onCreate={addGoal} />
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
