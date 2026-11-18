package no.kristiania.progressservice.progress;

import no.kristiania.progressservice.progress.dto.DateRange;
import no.kristiania.progressservice.progress.exceptions.UnsupportedTrackingException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class AmountPeriod {
    private AmountPeriod() {}

    public static DateRange current(String period, LocalDate goalStart, LocalDate today) {
        LocalDate anchor = (goalStart == null || goalStart.isAfter(today)) ? today : goalStart;
        ChronoUnit unit = switch (period) {
            case "WEEKLY" -> ChronoUnit.WEEKS;
            case "MONTHLY" -> ChronoUnit.MONTHS;
            case "YEARLY" -> ChronoUnit.YEARS;
            default -> throw new UnsupportedTrackingException(
                    "Unsupported period for AMOUNT: " + period);
        };
        long passed = unit.between(anchor, today);
        return new DateRange(anchor.plus(passed, unit),
                anchor.plus(passed + 1, unit).minusDays(1));
    }
}
