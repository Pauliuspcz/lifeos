import { useNavigate } from "react-router";
import BackButton from "../components/BackButton";

export default function ErrorPage() {
  const navigate = useNavigate();

  function handleBack() {
    navigate("/");
  }

  return (
    <div className="page">
      <h1>Fant ikke siden</h1>
      <p>Vi fant ikke siden du lette etter</p>
      <BackButton onBack={handleBack} />
    </div>
  );
}
