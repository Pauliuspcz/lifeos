import { defineConfig } from "vitest/config";
import { loadEnv } from "vite";

const gateway = loadEnv("", ".", "").GATEWAY_URL ?? "http://localhost:8100";

export default defineConfig({
  server: {
    proxy: {
      "/auth": gateway,
      "/profile/me": gateway,
      "/api/goal": gateway,
      "/api/progress": gateway,
      "/api/reports": gateway,
      "/api/catalog": gateway,
    },
  },
  test: {
    environment: "jsdom",
  },
});
