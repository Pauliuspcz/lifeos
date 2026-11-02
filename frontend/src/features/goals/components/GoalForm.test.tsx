import {
  cleanup,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ApiError } from "../../../shared/api/client";
import GoalForm from "./GoalForm";

afterEach(() => {
  cleanup();
});

function submitWithTitle(title: string) {
  fireEvent.change(screen.getByPlaceholderText("Title"), {
    target: { value: title },
  });
  fireEvent.click(screen.getByText("Create"));
}

describe("GoalForm", () => {
  it("shows a local error for a blank title and does not submit", () => {
    const onCreate = vi.fn();
    render(<GoalForm onCreate={onCreate} />);

    fireEvent.click(screen.getByText("Create"));

    expect(screen.getByText("Title is required")).toBeTruthy();
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

    expect(await screen.findByText("Could not create goal (500)")).toBeTruthy();
  });

  it("sends description, amount and unit", async () => {
    const onCreate = vi.fn().mockResolvedValue(undefined);
    render(<GoalForm onCreate={onCreate} />);

    fireEvent.change(screen.getByPlaceholderText("Title"), {
      target: { value: "Run" },
    });
    fireEvent.change(screen.getByPlaceholderText("Description (optional)"), {
      target: { value: "Easy run" },
    });
    fireEvent.change(screen.getByPlaceholderText("Amount per session"), {
      target: { value: "5" },
    });
    fireEvent.change(screen.getByPlaceholderText("Unit (e.g. km)"), {
      target: { value: "km" },
    });
    fireEvent.click(screen.getByText("Create"));

    await waitFor(() =>
      expect(onCreate).toHaveBeenCalledWith({
        title: "Run",
        description: "Easy run",
        period: "WEEKLY",
        timesPerPeriod: 1,
        amountPerSession: 5,
        unit: "km",
      }),
    );
  });

  it("sends null for empty optional fields", async () => {
    const onCreate = vi.fn().mockResolvedValue(undefined);
    render(<GoalForm onCreate={onCreate} />);

    submitWithTitle("Run");

    await waitFor(() =>
      expect(onCreate).toHaveBeenCalledWith({
        title: "Run",
        description: null,
        period: "WEEKLY",
        timesPerPeriod: 1,
        amountPerSession: null,
        unit: null,
      }),
    );
  });

  it("rejects a zero amount", () => {
    const onCreate = vi.fn();
    render(<GoalForm onCreate={onCreate} />);

    fireEvent.change(screen.getByPlaceholderText("Title"), {
      target: { value: "Run" },
    });
    fireEvent.change(screen.getByPlaceholderText("Amount per session"), {
      target: { value: "0" },
    });
    fireEvent.click(screen.getByText("Create"));

    expect(screen.getByText("Amount must be greater than 0")).toBeTruthy();
    expect(onCreate).not.toHaveBeenCalled();
  });
});
