import { request } from "../../shared/api/client";
import type { RelationHint, RelationRequest } from "./types";

export const fetchRelationHints = (body: RelationRequest) =>
  request<RelationHint[]>("POST", "/api/recommendations/goals", body, true);
