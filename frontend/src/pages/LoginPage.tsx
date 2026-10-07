import BackButton from "../components/BackButton";
import React from "react";
import { useNavigate } from "react-router";

export default function LoginPage() {
  const navigate = useNavigate();

  function handleBack() {
    navigate("/");
  }

  return (
    <>
      <h1>hello kris</h1>
      <BackButton onBack={handleBack} />
    </>
  );
}
