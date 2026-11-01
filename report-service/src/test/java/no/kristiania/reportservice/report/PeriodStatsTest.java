package no.kristiania.reportservice.report;

import no.kristiania.reportservice.report.dto.PeriodCount;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodStatsTest {
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
        assertThat(PeriodStats.currentStreak(List.of(), WEDNESDAY)).isZero();
    }

    @Test
    void weeklyCountsLastWeekWhenThisWeekIsNotDone() {
        List<ProgressRecord> entries = List.of(
                entry("2026-10-19", "WEEKLY", 3),
                entry("2026-10-21", "WEEKLY", 3),
                entry("2026-10-23", "WEEKLY", 3),
                entry("2026-10-26", "WEEKLY", 3));
        assertThat(PeriodStats.currentStreak(entries, WEDNESDAY)).isEqualTo(1);
    }

    @Test
    void weeklyBreaksOnWeekBelowTarget() {
        List<ProgressRecord> entries = List.of(
                entry("2026-10-12", "WEEKLY", 2),
                entry("2026-10-13", "WEEKLY", 2),
                entry("2026-10-20", "WEEKLY", 2),
                entry("2026-10-26", "WEEKLY", 2),
                entry("2026-10-27", "WEEKLY", 2));
        assertThat(PeriodStats.currentStreak(entries, WEDNESDAY)).isEqualTo(1);
    }

    @Test
    void dailyStartsYesterdayWhenTodayIsMissing() {
        List<ProgressRecord> entries = List.of(
                entry("2026-10-26", "DAILY", 1),
                entry("2026-10-27", "DAILY", 1));
        assertThat(PeriodStats.currentStreak(entries, WEDNESDAY)).isEqualTo(2);
    }

    @Test
    void missingPeriodFallsBackToDaily() {
        ProgressRecord old = new ProgressRecord();
        old.setDate(WEDNESDAY);
        assertThat(PeriodStats.currentStreak(List.of(old), WEDNESDAY)).isEqualTo(1);
    }
    @Test
    void successRateCountsReachedWeeks() {
        List<ProgressRecord> entries = List.of(
                entry("2026-10-12", "WEEKLY", 2), entry("2026-10-13", "WEEKLY", 2),
                entry("2026-10-20", "WEEKLY", 2),
                entry("2026-10-26", "WEEKLY", 2));
        assertThat(PeriodStats.successRate(entries, WEDNESDAY)).isEqualTo(50);
    }

    @Test
    void successRateIsZeroWithoutEntries() {
        assertThat(PeriodStats.successRate(List.of(), WEDNESDAY)).isZero();
    }

    @Test
    void monthlyCountsLastMonthWhenThisMonthIsNotDone() {
        List<ProgressRecord> entries = List.of(
                entry("2026-09-03", "MONTHLY", 2), entry("2026-09-20", "MONTHLY", 2),
                entry("2026-10-05", "MONTHLY", 2));
        assertThat(PeriodStats.currentStreak(entries, WEDNESDAY)).isEqualTo(1);
        assertThat(PeriodStats.successRate(entries, WEDNESDAY)).isEqualTo(100);
    }

    @Test
    void yearlyCurrentYearNotCountedUntilReached() {
        List<ProgressRecord> entries = List.of(
                entry("2026-03-01", "YEARLY", 3), entry("2026-06-01", "YEARLY", 3));
        assertThat(PeriodStats.successRate(entries, WEDNESDAY)).isZero();
    }

    @Test
    void historyReturnsLastPeriodsOldestFirst() {
        List<ProgressRecord> entries = List.of(
                entry("2026-10-19", "WEEKLY", 2), entry("2026-10-21", "WEEKLY", 2),
                entry("2026-10-26", "WEEKLY", 2));
        List<PeriodCount> history = PeriodStats.history(entries, WEDNESDAY, 3);

        assertThat(history).extracting(PeriodCount::periodStart).containsExactly(
                LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 26));
        assertThat(history).extracting(PeriodCount::count).containsExactly(0, 2, 1);
        assertThat(history).extracting(PeriodCount::reached).containsExactly(false, true, false);
    }
}
