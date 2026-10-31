import { defineConfig } from "vitest/config";

export default defineConfig({
  server: {
    proxy: {
      "/auth": "http://localhost:8080",
      "/profile/me": "http://localhost:8080",
      "/api/goal": "http://localhost:8081",
      "/api/progress": "http://localhost:8082",
      "/api/reports": "http://localhost:8083",
      "/api/catalog": "http://localhost:8081",
    },
  },
  test: {
    environment: "jsdom",
  },
});
