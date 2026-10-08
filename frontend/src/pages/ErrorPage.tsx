import { useNavigate } from "react-router";
import BackButton from "../components/BackButton";

export default function ErrorPage() {
  const navigate = useNavigate();

  function handleBack() {
    navigate("/");
  }

  return (
    <div>
      <h1>Page not found</h1>
      <p>We could not find the page you were looking for</p>
      <BackButton onBack={handleBack} />
    </div>
  );
}
