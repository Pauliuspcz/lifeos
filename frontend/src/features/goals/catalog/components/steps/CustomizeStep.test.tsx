import {
  cleanup,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ApiError } from "../../../../../shared/api/client";
import { addDays, todayIso } from "../../dateUtils";
import type { Category, Template } from "../../types";
import CustomizeStep from "./CustomizeStep";

afterEach(() => {
  cleanup();
});

const BODY: Category = {
  id: 1,
  code: "BODY",
  name: "Body",
  description: "Fitness, strength and movement",
  iconKey: "dumbbell",
  color: "#FF7043",
  sortOrder: 10,
};

function makeTemplate(overrides: Partial<Template> = {}): Template {
  return {
    id: 7,
    code: "RUN_3X_WEEK",
    categoryId: 1,
    directionId: 3,
    title: "Run 3 times a week",
    description: "A steady base for endurance",
    trackingType: "CHECK",
    defaultPeriod: "WEEKLY",
    defaultTimesPerPeriod: 3,
    defaultTargetAmount: null,
    defaultUnit: null,
    defaultTargetTime: null,
    defaultDurationDays: null,
    outdoor: true,
    difficulty: "MEDIUM",
    iconKey: "run",
    color: "#FF7043",
    ...overrides,
  };
}

function renderStep(
  template: Template | null,
  onCreate = vi.fn().mockResolvedValue(undefined),
) {
  render(<CustomizeStep area={BODY} template={template} onCreate={onCreate} />);
  return onCreate;
}

const clickCreate = () =>
  fireEvent.click(screen.getByRole("button", { name: "Create" }));

describe("CustomizeStep", () => {
  it("shows a local error for a blank title and does not submit", () => {
    const onCreate = renderStep(null);

    clickCreate();

    expect(screen.getByText("Title is required")).toBeTruthy();
    expect(onCreate).not.toHaveBeenCalled();
  });

  it("shows field errors from the backend", async () => {
    const onCreate = vi.fn().mockRejectedValue(
      new ApiError(400, "Validation failed", {
        title: "size must be <= 255",
      }),
    );
    renderStep(makeTemplate(), onCreate);

    clickCreate();

    expect(await screen.findByText("size must be <= 255")).toBeTruthy();
  });

  it("shows a general error when there are no field errors", async () => {
    const onCreate = vi
      .fn()
      .mockRejectedValue(new ApiError(500, "Server unavailable"));
    renderStep(makeTemplate(), onCreate);

    clickCreate();

    expect(await screen.findByText("Server unavailable")).toBeTruthy();
  });

  it("creates a check goal from a template in the area colour", async () => {
    const onCreate = renderStep(makeTemplate());

    clickCreate();

    await waitFor(() =>
      expect(onCreate).toHaveBeenCalledWith(
        expect.objectContaining({
          title: "Run 3 times a week",
          trackingType: "CHECK",
          period: "WEEKLY",
          timesPerPeriod: 3,
          targetAmount: null,
          unit: null,
          categoryId: 1,
          templateId: 7,
          color: "#FF7043",
          outdoor: true,
        }),
      ),
    );
  });

  it("sets the end date for a challenge from its duration", async () => {
    const onCreate = renderStep(
      makeTemplate({
        id: 20,
        code: "COLD_SHOWER_30_DAYS",
        title: "30-day cold shower challenge",
        defaultPeriod: "DAILY",
        defaultTimesPerPeriod: 1,
        defaultDurationDays: 30,
        outdoor: false,
      }),
    );

    clickCreate();

    await waitFor(() =>
      expect(onCreate).toHaveBeenCalledWith(
        expect.objectContaining({
          period: "DAILY",
          timesPerPeriod: 1,
          targetDate: addDays(todayIso(), 30),
        }),
      ),
    );
  });

  it("requires a future date for an amount goal by a date", () => {
    const onCreate = renderStep(
      makeTemplate({
        id: 8,
        code: "RUN_1000_KM",
        title: "Run 1,000 km by a date",
        trackingType: "AMOUNT",
        defaultPeriod: null,
        defaultTimesPerPeriod: null,
        defaultTargetAmount: 1000,
        defaultUnit: "km",
      }),
    );

    clickCreate();

    expect(screen.getByText("Pick a future date")).toBeTruthy();
    expect(onCreate).not.toHaveBeenCalled();
  });

  it("creates an amount goal with a target date and no period", async () => {
    const onCreate = renderStep(
      makeTemplate({
        id: 8,
        code: "RUN_1000_KM",
        title: "Run 1,000 km by a date",
        trackingType: "AMOUNT",
        defaultPeriod: null,
        defaultTimesPerPeriod: null,
        defaultTargetAmount: 1000,
        defaultUnit: "km",
      }),
    );

    fireEvent.change(screen.getByLabelText("Target date"), {
      target: { value: "2999-12-31" },
    });
    clickCreate();

    await waitFor(() =>
      expect(onCreate).toHaveBeenCalledWith(
        expect.objectContaining({
          trackingType: "AMOUNT",
          period: null,
          timesPerPeriod: null,
          targetAmount: 1000,
          unit: "km",
          targetDate: "2999-12-31",
        }),
      ),
    );
  });

  it("rejects a zero amount", () => {
    const onCreate = renderStep(null);

    fireEvent.change(screen.getByLabelText("How do you want to track it?"), {
      target: { value: "AMOUNT" },
    });
    fireEvent.change(screen.getByLabelText("Title"), {
      target: { value: "Run" },
    });
    fireEvent.change(screen.getByLabelText("Amount"), {
      target: { value: "0" },
    });
    fireEvent.change(screen.getByLabelText("Unit (e.g. km, books, reps)"), {
      target: { value: "km" },
    });
    clickCreate();

    expect(screen.getByText("Amount must be greater than 0")).toBeTruthy();
    expect(onCreate).not.toHaveBeenCalled();
  });
});
