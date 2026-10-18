import { Link, NavLink } from "react-router";

export default function Topbar() {
  return (
    <header className="topbar">
      <Link to="/frontpage" className="topbar-logo">
        <span className="logo-mark">✓</span>
        LifeOS
      </Link>
      <nav className="topbar-nav">
        <NavLink to="/">I dag</NavLink>
        <NavLink to="/goals">Mål</NavLink>
        <span>Innsikt</span>
        <span>Journal</span>
      </nav>
      <Link to="/profile" className="topbar-profile">
        Profil
      </Link>
    </header>
  );
}
