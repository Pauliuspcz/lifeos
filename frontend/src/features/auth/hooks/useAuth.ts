import { useSyncExternalStore } from "react";
import { tokenStore } from "../../../shared/api/tokens";

export function useAuth() {
  const isAuthenticated = useSyncExternalStore(
    tokenStore.subscribe,
    tokenStore.hasSession,
  );
  return { isAuthenticated };
}
