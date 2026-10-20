import { tokenStore, type AuthResponse } from "./tokens";

import { API_URL } from "./config";
import { REFRESH_PATH } from "./endpoints";

export type FieldErrors = Record<string, string>;

export class ApiError extends Error {
  status: number;
  fieldErrors: FieldErrors;

  constructor(status: number, message?: string, fieldErrors: FieldErrors = {}) {
    super(message ?? `HTTP ${status}`);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

async function toApiError(res: Response): Promise<ApiError> {
  const body = await res.json().catch(() => null);
  const message = typeof body?.message === "string" ? body.message : undefined;
  const fieldErrors =
    body?.fieldErrors && typeof body.fieldErrors === "object"
      ? (body.fieldErrors as FieldErrors)
      : undefined;
  return new ApiError(res.status, message, fieldErrors);
}

let refreshPromise: Promise<boolean> | null = null;

async function refreshTokens(): Promise<boolean> {
  const refreshToken = tokenStore.getRefresh();
  if (!refreshToken) return false;
  try {
    const res = await fetch(`${API_URL}${REFRESH_PATH}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });
    if (!res.ok) {
      if (res.status === 401) tokenStore.clear();
      return false;
    }
    tokenStore.save((await res.json()) as AuthResponse);
    return true;
  } catch {
    return false;
  }
}

function refreshOnce(): Promise<boolean> {
  refreshPromise ??= refreshTokens().finally(() => {
    refreshPromise = null;
  });
  return refreshPromise;
}

function send(method: string, path: string, body: unknown, auth: boolean) {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };
  if (auth) {
    const token = tokenStore.getAccess();
    if (token) headers.Authorization = `Bearer ${token}`;
  }
  return fetch(`${API_URL}${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });
}

export async function request<T>(
  method: string,
  path: string,
  body?: unknown,
  auth = false,
): Promise<T> {
  let res = await send(method, path, body, auth);

  if (res.status === 401 && auth && (await refreshOnce())) {
    res = await send(method, path, body, auth);
  }

  if (!res.ok) throw await toApiError(res);
  if (res.status === 204) return undefined as T;
  return (await res.json()) as Promise<T>;
}

export const post = <T>(path: string, body: unknown) =>
  request<T>("POST", path, body);
