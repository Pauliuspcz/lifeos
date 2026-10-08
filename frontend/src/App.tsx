import { BrowserRouter, Route, Routes } from "react-router";
import GoalAdd from "./pages/GoalAdd";
import ErrorPage from "./pages/ErrorPage";
import FrontPage from "./pages/FrontPage";
import LoginPage from "./pages/LoginPage";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path={"/"} element={<FrontPage />} />
        <Route path={"/login"} element={<LoginPage />} />
        <Route path={"/goal/new"} element={<GoalAdd />} />
        <Route path={"*"} element={<ErrorPage />} />
      </Routes>
    </BrowserRouter>
  );
}
