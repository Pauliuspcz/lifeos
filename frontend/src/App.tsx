import { BrowserRouter, Route, Routes } from "react-router";
import GoalAdd from "./pages/GoalAdd";
import ErrorPage from "./pages/ErrorPage";
import FrontPage from "./pages/FrontPage";
import LoginPage from "./pages/LoginPage";
import ProfilePage from "./pages/ProfilePage";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path={"/"} element={<FrontPage />} />
        <Route path={"/login"} element={<LoginPage />} />
        <Route path={"/profile"} element={<ProfilePage />} />
        <Route path={"/goal/new"} element={<GoalAdd />} />
        <Route path={"*"} element={<ErrorPage />} />
      </Routes>
    </BrowserRouter>
  );
}
