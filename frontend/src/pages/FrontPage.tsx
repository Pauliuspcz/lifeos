import { Link } from "react-router";

export default function FrontPage() {
  return (
    <div className="page">
      <h1>Velkommen til LifeOS :P</h1>
      <Link to="/">Til Dashbordet</Link>
    </div>
  );
}
