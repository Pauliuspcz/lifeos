import { Link, NavLink } from "react-router";

export default function Topbar() {
  return (
    <header className="topbar">
      <Link to="/dashboard" className="topbar-logo">
        <span className="logo-mark">✓</span>
        LifeOS
      </Link>
      <nav className="topbar-nav">
        <NavLink to="/dashboard">I dag</NavLink>
        <NavLink to="/goal/new">Mål</NavLink>
        <span>Innsikt</span>
        <span>Journal</span>
      </nav>
      <Link to="/profile" className="topbar-profile">
        Profil
      </Link>
    </header>
  );
}
