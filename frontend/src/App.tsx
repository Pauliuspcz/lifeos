import { BrowserRouter, Route, Routes } from "react-router";
import GoalAdd from "./pages/GoalAdd";
import ErrorPage from "./pages/ErrorPage";
import FrontPage from "./pages/FrontPage";
import LoginPage from "./pages/LoginPage";
import ProfilePage from "./pages/ProfilePage";
import { GoogleOAuthProvider } from "@react-oauth/google";
import DashPage from "./pages/DashPage";

export default function App() {
  return (
    <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>
      <BrowserRouter>
        <Routes>
          <Route path={"/"} element={<DashPage />} />
          <Route path={"/frontpage"} element={<FrontPage />} />
          <Route path={"/login"} element={<LoginPage />} />
          <Route path={"/profile"} element={<ProfilePage />} />
          <Route path={"/goal/new"} element={<GoalAdd />} />
          <Route path={"*"} element={<ErrorPage />} />
        </Routes>
      </BrowserRouter>
    </GoogleOAuthProvider>
  );
}
