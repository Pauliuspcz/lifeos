export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
};

const ACCESS = "accessToken";
const REFRESH = "refreshToken";

export const tokenStore = {
  save(auth: AuthResponse) {
    localStorage.setItem(ACCESS, auth.accessToken);
    localStorage.setItem(REFRESH, auth.refreshToken);
  },
  getAccess: () => localStorage.getItem(ACCESS),
  getRefresh: () => localStorage.getItem(REFRESH),
  clear() {
    localStorage.removeItem(ACCESS);
    localStorage.removeItem(REFRESH);
  },
};
