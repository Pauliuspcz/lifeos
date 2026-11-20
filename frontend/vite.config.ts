import { defineConfig } from "vitest/config";
import { loadEnv } from "vite";

const env = loadEnv("", "..", "");
const gateway = env.GATEWAY_URL ?? "http://localhost:8100";

export default defineConfig({
  define: {
    "import.meta.env.VITE_GOOGLE_CLIENT_ID": JSON.stringify(
      env.GOOGLE_CLIENT_ID ?? "",
    ),
  },
  server: {
    proxy: {
      "/auth": gateway,
      "/profile/me": gateway,
      "/api/goal": gateway,
      "/api/progress": gateway,
      "/api/reports": gateway,
      "/api/catalog": gateway,
      "/api/recommendations": gateway,
    },
  },
  test: {
    environment: "jsdom",
  },
});
