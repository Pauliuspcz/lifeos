import { useState, type SubmitEvent } from "react";
import { Navigate, useNavigate } from "react-router";
import { googleLogin, login, register, useAuth } from "../features/auth";
import { ApiError } from "../shared/api/client";
import { GoogleLogin } from "@react-oauth/google";
import { HOME_PATH } from "../shared/config/routes";
import logo from "../assets/logo-only.png";

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
  const { isAuthenticated } = useAuth();

  if (isAuthenticated) {
    return <Navigate to={HOME_PATH} replace />;
  }

  async function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      if (mode === "register") await register(email, password, name);
      await login(email, password);
      navigate(HOME_PATH);
    } catch (err) {
      setError(errorText(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth auth-screen">
      <img src={logo} alt="LifeOS" className="auth-logo" />
      <h1>{mode === "login" ? "Log in" : "Create account"}</h1>
      <form className="auth-form" onSubmit={handleSubmit}>
        {mode === "register" && (
          <label>
            Name
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </label>
        )}
        <label>
          Email
          <input
            type="email"
            placeholder="you@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </label>
        <label>
          Password
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </label>
        <button type="submit" className="btn-dark" disabled={loading}>
          {loading ? "…" : mode === "login" ? "Log in" : "Register"}
        </button>
      </form>
      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}
      <div className="auth-divider">or</div>
      <GoogleLogin
        text="continue_with"
        theme="outline"
        size="large"
        width={320}
        onSuccess={async (res) => {
          if (!res.credential) return;
          try {
            await googleLogin(res.credential);
            navigate(HOME_PATH);
          } catch (err) {
            setError(errorText(err));
          }
        }}
        onError={() => setError("Google login failed.")}
      />
      <p className="auth-switch">
        {mode === "login" ? "No account? " : "Have an account? "}
        <button
          type="button"
          className="link-btn"
          onClick={() => {
            setMode(mode === "login" ? "register" : "login");
            setError(null);
          }}
        >
          {mode === "login" ? "Register" : "Log in"}
        </button>
      </p>
    </div>
  );
}
