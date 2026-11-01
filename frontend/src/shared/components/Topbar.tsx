import { Link, NavLink } from "react-router";
import {
  GOAL_PATH,
  HOME_PATH,
  INSIGHT_PATH,
  LOG_PATH,
  PROFILE_PATH,
} from "../config/routes";

export default function Topbar() {
  return (
    <header className="topbar">
      <Link to={HOME_PATH} className="topbar-logo">
        LifeOS
      </Link>
      <nav className="topbar-nav">
        <NavLink to={HOME_PATH}>Today</NavLink>
        <NavLink to={INSIGHT_PATH}>Insights</NavLink>
        <NavLink to={GOAL_PATH}>Goals</NavLink>
        <NavLink to={LOG_PATH}>Log</NavLink>
        <span>Journal</span>
      </nav>
      <Link to={PROFILE_PATH} className="topbar-profile">
        Profile
      </Link>
    </header>
  );
}
