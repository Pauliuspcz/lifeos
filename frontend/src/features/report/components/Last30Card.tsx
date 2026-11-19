import type { Category, Goal } from "../../goals";
import type { ActivityReport, Wellbeing } from "../types";

type Props = {
  activity: ActivityReport | null;
  wellbeing: Wellbeing | null;
  streakGoals: Goal[];
  streakCount: number | undefined;
  categories: Category[];
  goalsError: string;
};

export default function Last30Card({
  activity,
  wellbeing,
  streakGoals,
  streakCount,
  categories,
  goalsError,
}: Props) {
  const days = activity?.days.slice(-30) ?? [];
  const activeDays = days.filter((d) => d.count > 0).length;
  const checkins = activity?.last30Days ?? 0;
  const mood = wellbeing?.avgMood ?? null;
  const logged = wellbeing?.daysLogged ?? 0;
  const colorOf = (g: Goal) =>
    categories.find((c) => c.id === g.categoryId)?.color ?? undefined;

  return (
    <section className="last30">
      <p className="activity-label">Last 30 days</p>
      <div className="last30-cols">
        <div className="last30-col">
          <h3>Consistency</h3>
          <p className="last30-hero">
            {activity ? activeDays : "…"}
            <span>of 30 days active</span>
          </p>
          <div className="last30-bar">
            {days.map((d) => (
              <span
                key={d.date}
                className={"last30-cell" + (d.count > 0 ? " on" : "")}
              />
            ))}
          </div>
          <p className="last30-note">
            {activeDays === 0 ? "No check-ins yet." : `${checkins} check-ins`}
          </p>
        </div>

        <div className="last30-col">
          <h3>Mood and sleep</h3>
          {logged === 0 ? (
            <p className="last30-note">
              No quick logs yet. Log mood and sleep on Today.
            </p>
          ) : (
            <>
              <div className="last30-row">
                <span>Mood</span>
                <span className="mood-dots">
                  {[1, 2, 3, 4, 5].map((n) => (
                    <span
                      key={n}
                      className={
                        "mood-dot" +
                        (mood != null && n <= Math.round(mood) ? " on" : "")
                      }
                    />
                  ))}
                </span>
                <strong>
                  {mood ?? "—"}
                  <span>/ 5</span>
                </strong>
              </div>
              <div className="last30-row">
                <span>Sleep</span>
                <strong>
                  {wellbeing?.avgSleep ?? "—"}
                  <span>h</span>
                </strong>
              </div>
              {logged < 7 && (
                <p className="last30-note">
                  From {logged} of 30 days. A few more logs will make these
                  averages meaningful.
                </p>
              )}
            </>
          )}
        </div>

        <div className="last30-col">
          <h3>Streaks</h3>
          <p className="last30-hero">
            {streakCount ?? "…"}
            <span>active{goalsError ? " · incl. archived" : ""}</span>
          </p>
          {goalsError ? (
            <p className="last30-note">Could not load active goals</p>
          ) : streakGoals.length === 0 ? (
            <p className="last30-note">No active streaks yet.</p>
          ) : (
            <ul className="last30-list">
              {streakGoals.map((g) => (
                <li key={g.id}>
                  <span
                    className="last30-dot"
                    style={{ background: colorOf(g) }}
                  />
                  {g.title}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </section>
  );
}
