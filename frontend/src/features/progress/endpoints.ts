export const progressEndpoints = {
  collection: "/api/progress",
  window: "/api/progress/window",
  byId: (id: number) => `/api/progress/${id}`,
} as const;
