package no.kristiania.progressservice.progress.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class Pace {
    private Pace() {}

    public static BigDecimal expected(BigDecimal target, LocalDate start,
                                      LocalDate end, LocalDate today) {
        if (target == null || start == null || end == null || end.isBefore(start)) {
            return null;
        }
        long totalDays = ChronoUnit.DAYS.between(start, end) + 1;
        long elapsed = Math.max(0, Math.min(totalDays,
                ChronoUnit.DAYS.between(start, today) + 1));
        return target.multiply(BigDecimal.valueOf(elapsed))
                .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);
    }
}
