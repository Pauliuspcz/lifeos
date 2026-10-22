import { request } from "../../shared/api/client";
import { profileEndpoints } from "./endpoints";

export type Profile = {
  id: number;
  email: string;
  name: string;
  profilePictureUrl: string | null;
};

export const getProfile = () =>
  request<Profile>("GET", profileEndpoints.me, undefined, true);

export const updateProfile = (data: {
  name?: string;
  profilePictureUrl?: string;
}) => request<Profile>("PATCH", profileEndpoints.me, data, true);
