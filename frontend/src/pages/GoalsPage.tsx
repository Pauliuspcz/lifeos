import { useGoals } from "../features/goals";
import BackButton from "../components/BackButton";
import { useNavigate } from "react-router";
import GoalForm from "../components/GoalForm";

export default function GoalsPage() {
  const navigate = useNavigate();
  const { goals, loading, loadError, addGoal } = useGoals();

  function handleBack() {
    navigate("/");
  }
  return (
    <div className="page">
      <h1>Mål</h1>
      <GoalForm onCreate={addGoal} />
      {loading && <p>Laster...</p>}
      {loadError && <p className="error">{loadError}</p>}
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
