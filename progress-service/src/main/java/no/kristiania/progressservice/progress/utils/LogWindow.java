package no.kristiania.progressservice.progress.utils;

import no.kristiania.progressservice.progress.entity.TrackingModel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public final class LogWindow {
    private LogWindow() {}

    public static LocalDate weekStart(LocalDate today) {
        return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate monthStart(LocalDate today) {
        return today.withDayOfMonth(1);
    }

    public static LocalDate earliest(TrackingModel model, LocalDate today) {
        return switch (model) {
            case DAILY_CHECK, CUMULATIVE_SUM -> today.minusDays(2);
            case WEEKLY_COUNT -> weekStart(today);
            case MONTHLY_COUNT -> {
                LocalDate weekAgo = today.minusDays(6);
                yield monthStart(today).isAfter(weekAgo) ? monthStart(today) : weekAgo;
            }
        };
    }

    public static boolean contains(TrackingModel model, LocalDate today, LocalDate date) {
        return !date.isBefore(earliest(model, today)) && !date.isAfter(today);
    }
}
