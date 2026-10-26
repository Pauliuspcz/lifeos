import BackButton from "../shared/components/BackButton";
import { useNavigate } from "react-router";
import { HOME_PATH } from "../shared/config/routes";
import { GoalForm, useGoals } from "../features/goals";
import Topbar from "../shared/components/Topbar";

export default function GoalsPage() {
  const navigate = useNavigate();
  // const { goals, loading, loadError, addGoal } = useGoals(); TODO: her er eventuelt alle mål hvis vi vil ha det
  const { goals, loading, loadError, addGoal } = useGoals("ACTIVE");

  function handleBack() {
    navigate(HOME_PATH);
  }
  return (
    <>
      <Topbar />
      <div className="page">
        <h1>Mål</h1>
        <GoalForm onCreate={addGoal} />
        {loading && <p>Laster...</p>}
        {loadError && <p className="error">{loadError}</p>}
        <ul>
          {goals.map((g) => (
            <li key={g.id}>
              {g.title} - {g.timesPerPeriod}x {g.period} ({g.status})
            </li>
          ))}
        </ul>
        <BackButton onBack={handleBack} />
      </div>
    </>
  );
}
