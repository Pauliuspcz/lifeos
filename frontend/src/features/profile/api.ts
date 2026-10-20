import { request } from "../../shared/api/client";

export type Api = {
  id: number;
  email: string;
  name: string;
  profilePictureUrl: string | null;
};

export const getProfile = () =>
  request<Api>("GET", "/profile/me", undefined, true);

export const updateProfile = (data: {
  name?: string;
  profilePictureUrl?: string;
}) => request<Api>("PATCH", "/profile/me", data, true);
