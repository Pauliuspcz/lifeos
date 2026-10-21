import { Link, NavLink } from "react-router";
import { GOAL_PATH, HOME_PATH, PROFILE_PATH } from "../config/routes";

export default function Topbar() {
  return (
    <header className="topbar">
      <Link to={HOME_PATH} className="topbar-logo">
        <span className="logo-mark">✓</span>
        LifeOS
      </Link>
      <nav className="topbar-nav">
        <NavLink to={HOME_PATH}>I dag</NavLink>
        <NavLink to={GOAL_PATH}>Mål</NavLink>
        <span>Innsikt</span>
        <span>Journal</span>
      </nav>
      <Link to={PROFILE_PATH} className="topbar-profile">
        Profil
      </Link>
    </header>
  );
}
