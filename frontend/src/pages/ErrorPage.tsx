import { useNavigate } from "react-router";
import BackButton from "../shared/components/BackButton";

export default function ErrorPage() {
  const navigate = useNavigate();

  function handleBack() {
    navigate("/");
  }

  return (
    <div className="page">
      <h1>Page not found</h1>
      <p>We couldn't find the page you were looking for</p>
      <BackButton onBack={handleBack} />
    </div>
  );
}
