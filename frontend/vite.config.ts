import { defineConfig } from "vite";

export default defineConfig({
    server: {
        proxy: {
            "/api/user": "http://localhost:8080",
            "/api/goal": "http://localhost:8081",
        },
    },
});