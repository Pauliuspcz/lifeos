export const weatherEndpoints = {
  current: (lat: number, lon: number) =>
    `/api/recommendations/weather?lat=${lat}&lon=${lon}`,
} as const;
