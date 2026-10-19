import { BrowserRouter, Route, Routes } from "react-router";
import GoalsPage from "./pages/GoalsPage";
import ErrorPage from "./pages/ErrorPage";
import FrontPage from "./pages/FrontPage";
import LoginPage from "./pages/LoginPage";
import ProfilePage from "./pages/ProfilePage";
import { GoogleOAuthProvider } from "@react-oauth/google";
import DashboardPage from "./pages/DashboardPage";

export default function App() {
  return (
    <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>
      <BrowserRouter>
        <Routes>
          <Route path={"/"} element={<DashboardPage />} />
          <Route path={"/frontpage"} element={<FrontPage />} />
          <Route path={"/login"} element={<LoginPage />} />
          <Route path={"/profile"} element={<ProfilePage />} />
          <Route path={"/goals"} element={<GoalsPage />} />
          <Route path={"*"} element={<ErrorPage />} />
        </Routes>
      </BrowserRouter>
    </GoogleOAuthProvider>
  );
}
