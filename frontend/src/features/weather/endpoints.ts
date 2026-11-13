export const weatherEndpoints = {
  current: (lat: number, lon: number) =>
    `/api/recommendations/weather?lat=${lat}&lon=${lon}`,
  mine: (lat: number, lon: number) =>
    `/api/recommendations/weather/me?lat=${lat}&lon=${lon}`,
} as const;
