package no.kristiania.progressservice.progress;

import no.kristiania.progressservice.client.TrackingModel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public final class LogWindow {
    private LogWindow() {}

    public static LocalDate weekStart(LocalDate today) {
        return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate earliest(TrackingModel model, LocalDate today) {
        return switch (model) {
            case DAILY_CHECK -> today.minusDays(2);
            case WEEKLY_COUNT, CUMULATIVE_SUM -> weekStart(today);
        };
    }

    public static boolean contains(TrackingModel model, LocalDate today, LocalDate date) {
        return !date.isBefore(earliest(model, today)) && !date.isAfter(today);
    }
}
