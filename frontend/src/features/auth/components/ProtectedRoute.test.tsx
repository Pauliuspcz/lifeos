import { act, cleanup, render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { tokenStore } from "../../../shared/api/tokens";
import { ProtectedRoute } from "./ProtectedRoute";

const auth = {
  accessToken: "a",
  refreshToken: "r",
  tokenType: "Bearer",
  expiresInSeconds: 3600,
};

function renderAtRoot() {
  render(
    <MemoryRouter initialEntries={["/"]}>
      <Routes>
        <Route path="/login" element={<p>login page</p>} />
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<p>secret page</p>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

beforeEach(() => {
  localStorage.clear();
});

afterEach(() => {
  cleanup();
});

describe("ProtectedRoute", () => {
  it("redirects to login without a session", () => {
    renderAtRoot();

    expect(screen.getByText("login page")).toBeTruthy();
    expect(screen.queryByText("secret page")).toBeNull();
  });

  it("renders the page with a session", () => {
    tokenStore.save(auth);

    renderAtRoot();

    expect(screen.getByText("secret page")).toBeTruthy();
  });

  it("redirects when the session ends while the page is open", () => {
    tokenStore.save(auth);
    renderAtRoot();

    act(() => {
      tokenStore.clear();
    });

    expect(screen.getByText("login page")).toBeTruthy();
  });
});
