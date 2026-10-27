import { describe, expect, it } from "vitest";
import type { Goal } from "../../goals";
import type { Progress } from "../../progress";
import { currentStreak } from "./streak";

const wednesday = new Date(2026, 9, 28);
const weekly = { id: 1, period: "WEEKLY", timesPerPeriod: 2 } as Goal;
const daily = { id: 2, period: "DAILY", timesPerPeriod: 1 } as Goal;

function checkins(goalId: number, dates: string[]): Progress[] {
  return dates.map((date, i) => ({
    id: i,
    goalId,
    date,
    amount: null,
    createdAt: "",
  }));
}

describe("currentStreak", () => {
  it("is 0 with no check-ins", () => {
    expect(currentStreak([], weekly, wednesday)).toBe(0);
  });

  it("counts this week when the target is reached", () => {
    const entries = checkins(1, ["2026-10-26", "2026-10-27"]);
    expect(currentStreak(entries, weekly, wednesday)).toBe(1);
  });

  it("does not break the streak when this week isn't done yet", () => {
    const entries = checkins(1, ["2026-10-26", "2026-10-20", "2026-10-22"]);
    expect(currentStreak(entries, weekly, wednesday)).toBe(1);
  });

  it("counts several weeks in a row", () => {
    const entries = checkins(1, [
      "2026-10-26",
      "2026-10-27",
      "2026-10-20",
      "2026-10-22",
      "2026-10-13",
      "2026-10-15",
    ]);
    expect(currentStreak(entries, weekly, wednesday)).toBe(3);
  });

  it("stops at a week where the target was missed", () => {
    const entries = checkins(1, [
      "2026-10-26",
      "2026-10-27",
      "2026-10-20",
      "2026-10-13",
      "2026-10-15",
    ]);
    expect(currentStreak(entries, weekly, wednesday)).toBe(1);
  });

  it("ignores check-ins for other goals", () => {
    const entries = checkins(99, ["2026-10-26", "2026-10-27"]);
    expect(currentStreak(entries, weekly, wednesday)).toBe(0);
  });

  it("DAILY counts days in a row, today not done yet", () => {
    const entries = checkins(2, ["2026-10-27", "2026-10-26", "2026-10-24"]);
    expect(currentStreak(entries, daily, wednesday)).toBe(2);
  });
});
