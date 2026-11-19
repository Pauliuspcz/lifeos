import { useEffect, useState, type SubmitEvent } from "react";
import { ApiError } from "../shared/api/client";
import { getProfile, updateProfile, type Profile } from "../features/profile";
import { logout } from "../features/auth";
import Topbar from "../shared/components/Topbar";

export default function ProfilePage() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [name, setName] = useState("");
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    getProfile()
      .then((p) => {
        setProfile(p);
        setName(p.name);
      })
      .catch(() => setMessage("Could not load profile."));
  }, []);

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
  }

  return (
    <>
      <Topbar />
      <div className="auth">
        <h1>Profile</h1>
        {!profile ? (
          <p className="muted">{message ?? "Loading…"}</p>
        ) : (
          <>
            <form className="auth-form" onSubmit={handleSave}>
              <label>
                Email
                <input value={profile.email} readOnly />
              </label>
              <label>
                Name
                <input
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </label>
              <button type="submit" className="btn-dark">
                Save
              </button>
            </form>
            {message && (
              <p className="auth-message" role="alert">
                {message}
              </p>
            )}
            <button
              type="button"
              className="btn-outline auth-logout"
              onClick={handleLogout}
            >
              Log out
            </button>
          </>
        )}
      </div>
    </>
  );
}
