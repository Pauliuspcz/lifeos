package no.kristiania.reportservice.report;

import no.kristiania.reportservice.report.dto.PeriodCount;
import no.kristiania.reportservice.report.dto.Suggestion;
import no.kristiania.reportservice.report.enums.Recommendation;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
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

    private static List<ProgressRecord> weeks(int target, int... perWeek) {
        List<ProgressRecord> entries = new ArrayList<>();
        LocalDate monday = LocalDate.of(2026, 9, 28);
        for (int count : perWeek) {
            for (int day = 0; day < count; day++) {
                entries.add(entry(monday.plusDays(day).toString(), "WEEKLY", target));
            }
            monday = monday.plusWeeks(1);
        }
        return entries;
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

    @Test
    void suggestsRaiseWhenAllFinishedWeeksReached() {
        assertThat(PeriodStats.suggestion(weeks(2, 2, 2, 2, 2), WEDNESDAY))
                .isEqualTo(new Suggestion(Recommendation.RAISE, 3));
    }

    @Test
    void suggestsKeepWhenOffByOne() {
        assertThat(PeriodStats.suggestion(weeks(2, 2, 1, 2, 1), WEDNESDAY))
                .isEqualTo(new Suggestion(Recommendation.KEEP, null));
    }

    @Test
    void suggestsLowerToTypicalPlusOne() {
        assertThat(PeriodStats.suggestion(weeks(4, 1, 1, 0, 2), WEDNESDAY))
                .isEqualTo(new Suggestion(Recommendation.LOWER, 2));
    }

    @Test
    void neverRaisesAboveWeeklyMax() {
        assertThat(PeriodStats.suggestion(weeks(7, 7, 7, 7, 7), WEDNESDAY))
                .isEqualTo(new Suggestion(Recommendation.KEEP, null));
    }

    @Test
    void noneWhenGoalIsYoungerThanWindow() {
        List<ProgressRecord> entries = List.of(
                entry("2026-10-12", "WEEKLY", 2), entry("2026-10-13", "WEEKLY", 2),
                entry("2026-10-19", "WEEKLY", 2), entry("2026-10-20", "WEEKLY", 2));
        assertThat(PeriodStats.suggestion(entries, WEDNESDAY).recommendation())
                .isEqualTo(Recommendation.NONE);
    }

    @Test
    void onlyCountsWeeksAtCurrentTarget() {
        List<ProgressRecord> entries = new ArrayList<>(weeks(2, 2, 2, 2, 2));
        entries.add(entry("2026-10-26", "WEEKLY", 3));
        assertThat(PeriodStats.suggestion(entries, WEDNESDAY).recommendation())
                .isEqualTo(Recommendation.NONE);
    }

    @Test
    void noneForDailyGoals() {
        List<ProgressRecord> entries = List.of(entry("2026-10-27", "DAILY", 1));
        assertThat(PeriodStats.suggestion(entries, WEDNESDAY).recommendation())
                .isEqualTo(Recommendation.NONE);
    }

    @Test
    void noneWithoutEntries() {
        assertThat(PeriodStats.suggestion(List.of(), WEDNESDAY).recommendation())
                .isEqualTo(Recommendation.NONE);
    }

    @Test
    void monthlyRaisesAfterThreeReachedMonths() {
        List<ProgressRecord> entries = List.of(
                entry("2026-07-01", "MONTHLY", 2), entry("2026-07-02", "MONTHLY", 2),
                entry("2026-08-01", "MONTHLY", 2), entry("2026-08-02", "MONTHLY", 2),
                entry("2026-09-01", "MONTHLY", 2), entry("2026-09-02", "MONTHLY", 2));
        assertThat(PeriodStats.suggestion(entries, WEDNESDAY))
                .isEqualTo(new Suggestion(Recommendation.RAISE, 3));
    }
}
