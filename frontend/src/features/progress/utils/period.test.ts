import { describe, expect, it } from "vitest";
import type { Goal } from "../../goals";
import { countInPeriod, periodStart } from "./period";
import type { Progress } from "../types";

const monday = new Date(2026, 9, 26);
const wednesday = new Date(2026, 9, 28);
const sunday = new Date(2026, 10, 1);

describe("periodStart", () => {
  it("DAILY starts today", () => {
    expect(periodStart("DAILY", wednesday)).toBe("2026-10-28");
  });

  it("WEEKLY starts on Monday", () => {
    expect(periodStart("WEEKLY", wednesday)).toBe("2026-10-26");
  });

  it("WEEKLY on a Monday starts the same day", () => {
    expect(periodStart("WEEKLY", monday)).toBe("2026-10-26");
  });

  it("WEEKLY on a Sunday goes back to Monday in the previous month", () => {
    expect(periodStart("WEEKLY", sunday)).toBe("2026-10-26");
  });

  it("MONTHLY starts on the 1st", () => {
    expect(periodStart("MONTHLY", wednesday)).toBe("2026-10-01");
  });

  it("YEARLY starts on January 1st", () => {
    expect(periodStart("YEARLY", wednesday)).toBe("2026-01-01");
  });
});

const goal = (id: number, period: Goal["period"] = "WEEKLY"): Goal => ({
  id,
  title: `Goal ${id}`,
  description: null,
  period,
  timesPerPeriod: 3,
  amountPerSession: null,
  unit: null,
  status: "ACTIVE",
  templateId: null,
  version: 0,
  createdAt: "",
  updatedAt: "",
  archivedAt: null,
  goalType: "HABIT",
  trackingType: "COUNT",
  targetTime: null,
  startDate: null,
  targetDate: null,
  motivation: null,
  iconKey: null,
  color: null,
  categoryId: null,
});

const entry = (id: number, goalId: number, date: string): Progress => ({
  id,
  goalId,
  date,
  amount: null,
  createdAt: "",
});

describe("countInPeriod", () => {
  it("counts this week's entries for the goal", () => {
    const entries = [entry(1, 1, "2026-10-26"), entry(2, 1, "2026-10-28")];

    expect(countInPeriod(entries, goal(1), wednesday)).toBe(2);
  });

  it("ignores entries from last week", () => {
    const entries = [entry(1, 1, "2026-10-25"), entry(2, 1, "2026-10-27")];

    expect(countInPeriod(entries, goal(1), wednesday)).toBe(1);
  });

  it("ignores entries for other goals", () => {
    const entries = [entry(1, 1, "2026-10-27"), entry(2, 2, "2026-10-27")];

    expect(countInPeriod(entries, goal(1), wednesday)).toBe(1);
  });

  it("DAILY only counts today", () => {
    const entries = [entry(1, 1, "2026-10-27"), entry(2, 1, "2026-10-28")];

    expect(countInPeriod(entries, goal(1, "DAILY"), wednesday)).toBe(1);
  });
});
