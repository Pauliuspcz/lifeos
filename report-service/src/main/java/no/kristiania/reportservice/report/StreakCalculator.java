package no.kristiania.reportservice.report;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public final class StreakCalculator {
    private StreakCalculator() {}

    public static int currentStreak(List<ProgressRecord> entries, LocalDate today) {
        ProgressRecord latest = entries.isEmpty() ? null : entries.getLast();
        String period = (latest == null || latest.getPeriod() == null) ? "DAILY" : latest.getPeriod();
        int target = (latest == null || latest.getTargetCount() == null) ? 1 : latest.getTargetCount();
        Period unit = unitOf(period);
        LocalDate start = periodStart(period, today);

        if (countIn(entries, start, unit) < target) start = start.minus(unit);
        int streak = 0;
        while (countIn(entries, start, unit) >= target) {
            streak++;
            start = start.minus(unit);
        }
        return streak;
    }

    static Period unitOf(String period) {
        return switch (period) {
            case "WEEKLY" -> Period.ofWeeks(1);
            case "MONTHLY" -> Period.ofMonths(1);
            case "YEARLY" -> Period.ofYears(1);
            default -> Period.ofDays(1);
        };
    }

    static LocalDate periodStart(String period, LocalDate today) {
        return switch (period) {
            case "WEEKLY" -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case "MONTHLY" -> today.withDayOfMonth(1);
            case "YEARLY" -> today.withDayOfYear(1);
            default -> today;
        };
    }

    static int countIn(List<ProgressRecord> entries, LocalDate start, Period unit) {
        LocalDate end = start.plus(unit);
        int count = 0;
        for (ProgressRecord entry : entries) {
            if (!entry.getDate().isBefore(start) && entry.getDate().isBefore(end)) count++;
        }
        return count;
    }
}
