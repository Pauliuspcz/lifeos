package no.kristiania.progressservice.unit;

import no.kristiania.progressservice.progress.utils.AmountPeriod;
import no.kristiania.progressservice.progress.dto.DateRange;
import no.kristiania.progressservice.exceptions.UnsupportedTrackingException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AmountPeriodTest {
    private static final LocalDate START = LocalDate.of(2026, 11, 20);

    private static DateRange range(String from, String to) {
        return new DateRange(LocalDate.parse(from), LocalDate.parse(to));
    }

    @Test
    void monthlyStartsOnGoalStartDate() {
        assertThat(AmountPeriod.current("MONTHLY", START, LocalDate.of(2026, 11, 25)))
                .isEqualTo(range("2026-11-20", "2026-12-19"));
    }

    @Test
    void lastDayStaysInPeriod() {
        assertThat(AmountPeriod.current("MONTHLY", START, LocalDate.of(2026, 12, 19)))
                .isEqualTo(range("2026-11-20", "2026-12-19"));
    }

    @Test
    void monthlyRollsToNextPeriod() {
        assertThat(AmountPeriod.current("MONTHLY", START, LocalDate.of(2026, 12, 20)))
                .isEqualTo(range("2026-12-20", "2027-01-19"));
    }

    @Test
    void monthEndStartDoesNotDrift() {
        LocalDate jan31 = LocalDate.of(2026, 1, 31);
        assertThat(AmountPeriod.current("MONTHLY", jan31, LocalDate.of(2026, 3, 29)))
                .isEqualTo(range("2026-02-28", "2026-03-30"));
        assertThat(AmountPeriod.current("MONTHLY", jan31, LocalDate.of(2026, 3, 31)))
                .isEqualTo(range("2026-03-31", "2026-04-29"));
    }

    @Test
    void weeklyCountsFromStartDay() {
        LocalDate wednesday = LocalDate.of(2026, 11, 18);
        assertThat(AmountPeriod.current("WEEKLY", wednesday, LocalDate.of(2026, 11, 24)))
                .isEqualTo(range("2026-11-18", "2026-11-24"));
        assertThat(AmountPeriod.current("WEEKLY", wednesday, LocalDate.of(2026, 11, 25)))
                .isEqualTo(range("2026-11-25", "2026-12-01"));
    }

    @Test
    void missingStartDateUsesToday() {
        LocalDate today = LocalDate.of(2026, 11, 18);
        assertThat(AmountPeriod.current("MONTHLY", null, today))
                .isEqualTo(range("2026-11-18", "2026-12-17"));
    }

    @Test
    void unsupportedPeriodThrows() {
        assertThatThrownBy(() -> AmountPeriod.current("DAILY", START, START))
                .isInstanceOf(UnsupportedTrackingException.class);
    }
}