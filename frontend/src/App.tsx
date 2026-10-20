import { BrowserRouter, Route, Routes } from "react-router";
import GoalsPage from "./pages/GoalsPage";
import ErrorPage from "./pages/ErrorPage";
import FrontPage from "./pages/FrontPage";
import LoginPage from "./pages/LoginPage";
import ProfilePage from "./pages/ProfilePage";
import { GoogleOAuthProvider } from "@react-oauth/google";
import DashboardPage from "./pages/DashboardPage";
import { ProtectedRoute } from "./features/auth";
import {
  GOAL_PATH,
  HOME_PATH,
  LOGIN_PATH,
  PROFILE_PATH,
} from "./shared/config/routes";

export default function App() {
  return (
    <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>
      <BrowserRouter>
        <Routes>
          <Route path={"/frontpage"} element={<FrontPage />} />
          <Route path={LOGIN_PATH} element={<LoginPage />} />
          <Route element={<ProtectedRoute />}>
            <Route path={HOME_PATH} element={<DashboardPage />} />
            <Route path={PROFILE_PATH} element={<ProfilePage />} />
            <Route path={GOAL_PATH} element={<GoalsPage />} />
          </Route>
          <Route path={"*"} element={<ErrorPage />} />
        </Routes>
      </BrowserRouter>
    </GoogleOAuthProvider>
  );
}
