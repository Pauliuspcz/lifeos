import { post } from "./Client";
import { tokenStore, type AuthResponse } from "./Tokens";

export async function login(email: string, password: string) {
  const auth = await post<AuthResponse>("/auth/login", { email, password });
  tokenStore.save(auth);
  return auth;
}

export async function register(email: string, password: string, name: string) {
  await post("/auth/register", { email, password, name });
}
