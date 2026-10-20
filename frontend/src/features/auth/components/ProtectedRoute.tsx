import { Navigate, Outlet } from "react-router";
import { useAuth } from "../hooks/useAuth";
import { LOGIN_PATH } from "../../../shared/config/routes";

export function ProtectedRoute() {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to={LOGIN_PATH} replace />;
  }

  return <Outlet />;
}
