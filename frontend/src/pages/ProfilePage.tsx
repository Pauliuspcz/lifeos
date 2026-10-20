import React, { useEffect, useState, type SubmitEvent } from "react";
import { useNavigate } from "react-router";
import { ApiError } from "../shared/api/client";
import { getProfile, updateProfile, type Api } from "../features/profile/api";
import BackButton from "../components/BackButton";
import { logout } from "../features/auth/api";

export default function ProfilePage() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState<Api | null>(null);
  const [name, setName] = useState("");
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    getProfile()
      .then((p) => {
        setProfile(p);
        setName(p.name);
      })
      .catch((err) => {
        if (err instanceof ApiError && err.status === 401) navigate("/login");
        else setMessage("Could not load profile.");
      });
  }, [navigate]);

  async function handleSave(e: SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    setMessage(null);
    try {
      const updated = await updateProfile({ name });
      setProfile(updated);
      setMessage("Saved!");
    } catch (err) {
      setMessage(
        err instanceof ApiError && err.status === 400
          ? "Name can't be empty."
          : "Could not save.",
      );
    }
  }

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  function handleBack() {
    navigate("/");
  }

  if (!profile) return <div className="page">{message ?? "Loading..."}</div>;

  return (
    <div className="page">
      <h1>Profile</h1>
      <p>{profile.email}</p>
      <form onSubmit={handleSave}>
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <button type="submit">Save</button>
      </form>
      {message && <p role="alert">{message}</p>}
      <button type="button" onClick={handleLogout}>
        Log out
      </button>
      <BackButton onBack={handleBack} />
    </div>
  );
}
