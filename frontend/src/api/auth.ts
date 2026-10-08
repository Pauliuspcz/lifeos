import { post } from "./client";
import { tokenStore, type AuthResponse } from "./tokens";

export async function login(email: string, password: string) {
  const auth = await post<AuthResponse>("/auth/login", { email, password });
  tokenStore.save(auth);
  return auth;
}

export async function register(email: string, password: string, name: string) {
  await post("/auth/register", { email, password, name });
}

export async function googleLogin(idToken: string) {
  const auth = await post<AuthResponse>("/auth/google", { idToken });
  tokenStore.save(auth);
  return auth;
}
