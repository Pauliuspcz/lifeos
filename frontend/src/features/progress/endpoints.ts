export const progressEndpoints = {
  collection: "/api/progress",
  byId: (id: number) => `/api/progress/${id}`,
} as const;
