import { Link } from "react-router";

export default function FrontPage() {
  return (
    <>
      <div className="page">
        <h1>Welcome to LifeOS :P</h1>
        <Link to="/">Dashboard</Link>
      </div>
    </>
  );
}
