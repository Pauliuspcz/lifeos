import React, { useEffect, useState, type SubmitEvent } from "react";
import { useNavigate } from "react-router";
import { ApiError } from "../api/Client";
import { getProfile, updateProfile, type Profile } from "../api/Profile";
import { tokenStore } from "../api/Tokens";
import BackButton from "../components/BackButton";

export default function ProfilePage() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState<Profile | null>(null);
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

  function handleLogout() {
    tokenStore.clear();
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
