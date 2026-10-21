import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ApiError } from "../../../shared/api/client";
import GoalForm from "./GoalForm";

afterEach(() => {
  cleanup();
});

function submitWithTitle(title: string) {
  fireEvent.change(screen.getByPlaceholderText("Tittel"), {
    target: { value: title },
  });
  fireEvent.click(screen.getByText("Opprett"));
}

describe("GoalForm", () => {
  it("shows a local error for a blank title and does not submit", () => {
    const onCreate = vi.fn();
    render(<GoalForm onCreate={onCreate} />);

    fireEvent.click(screen.getByText("Opprett"));

    expect(screen.getByText("Tittel må fylles ut")).toBeTruthy();
    expect(onCreate).not.toHaveBeenCalled();
  });

  it("shows field errors from the backend", async () => {
    const onCreate = vi.fn().mockRejectedValue(
      new ApiError(400, "Validation failed", {
        title: "size must be <= 255",
      }),
    );
    render(<GoalForm onCreate={onCreate} />);

    submitWithTitle("x");

    expect(await screen.findByText("size must be <= 255")).toBeTruthy();
  });

  it("shows a general error when there are no field errors", async () => {
    const onCreate = vi.fn().mockRejectedValue(new ApiError(500));
    render(<GoalForm onCreate={onCreate} />);

    submitWithTitle("x");

    expect(
      await screen.findByText("Kunne ikke opprette mål (500)"),
    ).toBeTruthy();
  });
});
