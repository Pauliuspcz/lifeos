import { post } from "../../shared/api/client";
import { tokenStore, type AuthResponse } from "../../shared/api/tokens";
import { authEndpoints } from "./endpoints";

export async function login(email: string, password: string) {
  const auth = await post<AuthResponse>(`${authEndpoints.login}`, {
    email,
    password,
  });
  tokenStore.save(auth);
  return auth;
}

export async function register(email: string, password: string, name: string) {
  await post(`${authEndpoints.register}`, { email, password, name });
}

export async function googleLogin(idToken: string) {
  const auth = await post<AuthResponse>(`${authEndpoints.google}`, { idToken });
  tokenStore.save(auth);
  return auth;
}
export async function logout() {
  const refreshToken = tokenStore.getRefresh();
  tokenStore.clear();
  if (refreshToken) {
    try {
      await post(`${authEndpoints.logout}`, { refreshToken });
    } catch {}
  }
}
