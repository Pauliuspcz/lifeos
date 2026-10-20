import { request } from "../../shared/api/client";
import { profileEndpoints } from "./endpoints";

export type Api = {
  id: number;
  email: string;
  name: string;
  profilePictureUrl: string | null;
};

export const getProfile = () =>
  request<Api>("GET", `${profileEndpoints.me}`, undefined, true);

export const updateProfile = (data: {
  name?: string;
  profilePictureUrl?: string;
}) => request<Api>("PATCH", `${profileEndpoints.me}`, data, true);
