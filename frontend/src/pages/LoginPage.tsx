import { useState, type SubmitEvent } from "react";
import { useNavigate } from "react-router";
import { login, register } from "../api/Auth";
import { ApiError } from "../api/Client";

function errorText(err: unknown): string {
  if (err instanceof ApiError) {
    if (err.status === 400)
      return "Check the fields (password must be at least 8 characters).";
    if (err.status === 401) return "Wrong email or password.";
    if (err.status === 409) return "Email is already registered.";
  }
  return "Something went wrong. Is the backend running?";
}

export default function LoginPage() {
  const navigate = useNavigate();
  const [mode, setMode] = useState<"login" | "register">("login");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      if (mode === "register") await register(email, password, name);
      await login(email, password);
      navigate("/");
    } catch (err) {
      setError(errorText(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <h1>{mode === "login" ? "Log in" : "Create account"}</h1>
      <form onSubmit={handleSubmit}>
        {mode === "register" && (
          <input
            placeholder="Name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
        )}
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit" disabled={loading}>
          {loading ? "..." : mode === "login" ? "Log in" : "Register"}
        </button>
      </form>
      {error && <p role="alert">{error}</p>}
      <button
        type="button"
        onClick={() => setMode(mode === "login" ? "register" : "login")}
      >
        {mode === "login" ? "No account? Register" : "Have an account? Log in"}
      </button>
    </div>
  );
}
