import type { Category, Direction, Template } from "./types";
import { request } from "../../../shared/api/client";

export const listDirections = () =>
  request<Direction[]>("GET", "/api/catalog/directions", undefined, false);
export const listDirectionTemplates = (id: number) =>
  request<Template[]>(
    "GET",
    `/api/catalog/directions/${id}/templates`,
    undefined,
    false,
  );
export const listCategories = () =>
  request<Category[]>("GET", "/api/catalog/categories", undefined, false);
export const listCategoryTemplates = (id: number) =>
  request<Template[]>(
    "GET",
    `/api/catalog/categories/${id}/templates`,
    undefined,
    false,
  );
