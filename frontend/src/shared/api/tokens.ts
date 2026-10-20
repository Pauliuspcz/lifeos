export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
};

const ACCESS = "accessToken";
const REFRESH = "refreshToken";

const listeners = new Set<() => void>();

function notify() {
  listeners.forEach((listener) => listener());
}

export const tokenStore = {
  save(auth: AuthResponse) {
    localStorage.setItem(ACCESS, auth.accessToken);
    localStorage.setItem(REFRESH, auth.refreshToken);
    notify();
  },
  getAccess: () => localStorage.getItem(ACCESS),
  getRefresh: () => localStorage.getItem(REFRESH),
  hasSession: () => localStorage.getItem(REFRESH) !== null,
  clear() {
    localStorage.removeItem(ACCESS);
    localStorage.removeItem(REFRESH);
    notify();
  },
  subscribe(listener: () => void) {
    listeners.add(listener);
    window.addEventListener("storage", listener);
    return () => {
      listeners.delete(listener);
      window.removeEventListener("storage", listener);
    };
  },
};
