import { format, parseISO } from "date-fns";
import { useActivity } from "../hooks/useActivity";

export default function ActivityCalendar() {
  const { activity, loadError } = useActivity();
  const days = activity?.days ?? [];
  const total = activity?.totalCheckins ?? 0;
  const active = activity?.activeDays ?? 0;

  return (
    <section className="activity">
      <p className="activity-label">Activity</p>
      <p className="activity-total">
        {total} {total === 1 ? "check-in" : "check-ins"}
        <span>
          {active} {active === 1 ? "active day" : "active days"}
        </span>
      </p>
      {loadError && <p className="error">{loadError}</p>}
      <div className="activity-grid">
        {days.map((d) => (
          <div
            key={d.date}
            className={`activity-day level-${d.level}`}
            title={`${d.count} ${d.count === 1 ? "check-in" : "check-ins"} on ${format(parseISO(d.date), "d MMM")}`}
          />
        ))}
      </div>
      {days.length > 0 && (
        <div className="activity-range">
          <span>{format(parseISO(days[0].date), "MMM yyyy")}</span>
          <span>Today</span>
        </div>
      )}
      <p className="activity-note">Includes archived goals</p>
    </section>
  );
}
