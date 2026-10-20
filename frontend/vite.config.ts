import { defineConfig } from "vitest/config";

export default defineConfig({
  server: {
    proxy: {
      "/api/user": "http://localhost:8080",
      "/api/goal": "http://localhost:8081",
      "/api/progress": "http://localhost:8082",
    },
  },
  test: {
    environment: "jsdom",
  },
});
