package no.kristiania.reportservice.report;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StreakCalculatorTest {
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 10, 28);

    private static ProgressRecord entry(String date, String period, int target) {
        ProgressRecord r = new ProgressRecord();
        r.setDate(LocalDate.parse(date));
        r.setPeriod(period);
        r.setTargetCount(target);
        return r;
    }

    @Test
    void noEntriesGivesZero() {
        assertThat(StreakCalculator.currentStreak(List.of(), WEDNESDAY)).isZero();
    }

    @Test
    void weeklyCountsLastWeekWhenThisWeekIsNotDone() {
        List<ProgressRecord> entries = List.of(
                entry("2026-10-19", "WEEKLY", 3),
                entry("2026-10-21", "WEEKLY", 3),
                entry("2026-10-23", "WEEKLY", 3),
                entry("2026-10-26", "WEEKLY", 3));
        assertThat(StreakCalculator.currentStreak(entries, WEDNESDAY)).isEqualTo(1);
    }

    @Test
    void weeklyBreaksOnWeekBelowTarget() {
        List<ProgressRecord> entries = List.of(
                entry("2026-10-12", "WEEKLY", 2),
                entry("2026-10-13", "WEEKLY", 2),
                entry("2026-10-20", "WEEKLY", 2),
                entry("2026-10-26", "WEEKLY", 2),
                entry("2026-10-27", "WEEKLY", 2));
        assertThat(StreakCalculator.currentStreak(entries, WEDNESDAY)).isEqualTo(1);
    }

    @Test
    void dailyStartsYesterdayWhenTodayIsMissing() {
        List<ProgressRecord> entries = List.of(
                entry("2026-10-26", "DAILY", 1),
                entry("2026-10-27", "DAILY", 1));
        assertThat(StreakCalculator.currentStreak(entries, WEDNESDAY)).isEqualTo(2);
    }

    @Test
    void missingPeriodFallsBackToDaily() {
        ProgressRecord old = new ProgressRecord();
        old.setDate(WEDNESDAY);
        assertThat(StreakCalculator.currentStreak(List.of(old), WEDNESDAY)).isEqualTo(1);
    }
}
