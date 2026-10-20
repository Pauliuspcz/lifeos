import { request } from "../shared/api/client";

export type Profile = {
  id: number;
  email: string;
  name: string;
  profilePictureUrl: string | null;
};

export const getProfile = () =>
  request<Profile>("GET", "/profile/me", undefined, true);

export const updateProfile = (data: {
  name?: string;
  profilePictureUrl?: string;
}) => request<Profile>("PATCH", "/profile/me", data, true);
