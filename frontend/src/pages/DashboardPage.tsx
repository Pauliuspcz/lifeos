import Topbar from "../shared/components/Topbar";
import { GoalsCard, useCategories, useGoals } from "../features/goals";
import {
  QuickLogCard,
  StreaksCard,
  useActivity,
  useGoalReports,
} from "../features/report";
import {
  LogProgressCard,
  CheckinCard,
  useAmountSummaries,
  useProgress,
} from "../features/progress";
import { countInPeriod } from "../features/progress";
import { OutdoorWeatherCard } from "../features/weather";

export default function DashboardPage() {
  const { goals, loading, loadError, addGoal, archiveGoal } =
    useGoals("ACTIVE");
  const { entries, addProgress, removeProgress } = useProgress();
  const { reports } = useGoalReports(goals, entries);
  const { activity } = useActivity(entries);
  const categories = useCategories();
  const summaries = useAmountSummaries(goals, entries);

  return (
    <>
      <Topbar />
      <div className="dashboard">
        <div className="dash-col">
          <CheckinCard
            goals={goals}
            categories={categories}
            entries={entries}
            onCheck={addProgress}
            onUncheck={removeProgress}
            loadError={loadError}
          />
          <LogProgressCard
            goals={goals}
            summaries={summaries}
            onLog={addProgress}
            loadError={loadError}
          />
          <QuickLogCard />
        </div>
        <div className="dash-col">
          <GoalsCard
            goals={goals}
            loading={loading}
            loadError={loadError}
            onCreate={addGoal}
            onArchive={archiveGoal}
            countFor={(g) => countInPeriod(entries, g)}
            totalFor={(g) => summaries[g.id]?.total}
          />
          <OutdoorWeatherCard goals={goals} />
          <StreaksCard
            goals={goals}
            reports={reports}
            count={activity?.activeStreaks ?? null}
            loadError={loadError}
          />
        </div>
      </div>
    </>
  );
}
