import { defineConfig } from "vitest/config";

export default defineConfig({
  server: {
    proxy: {
      "/auth": "http://localhost:8100",
      "/profile/me": "http://localhost:8100",
      "/api/goal": "http://localhost:8100",
      "/api/progress": "http://localhost:8100",
      "/api/reports": "http://localhost:8100",
      "/api/catalog": "http://localhost:8100",
    },
  },
  test: {
    environment: "jsdom",
  },
});
