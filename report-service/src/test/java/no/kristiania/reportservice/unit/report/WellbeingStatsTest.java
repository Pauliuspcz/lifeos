package no.kristiania.reportservice.unit.report;

import no.kristiania.reportservice.report.ProgressRecord;
import no.kristiania.reportservice.report.daylog.DayLog;
import no.kristiania.reportservice.report.daylog.WellbeingStats;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WellbeingStatsTest {

    private static DayLog log(String date, Integer sleepHours) {
        DayLog log = new DayLog();
        log.setDate(LocalDate.parse(date));
        log.setSleepHours(sleepHours);
        return log;
    }

    private static ProgressRecord record(long goalId, String date) {
        ProgressRecord r = new ProgressRecord();
        r.setGoalId(goalId);
        r.setDate(LocalDate.parse(date));
        return r;
    }

    @Test
    void averageSkipsEmptyValues() {
        assertThat(WellbeingStats.average(Arrays.asList(4, null, 3))).isEqualTo(3.5);
    }

    @Test
    void averageOfNothingIsNull() {
        assertThat(WellbeingStats.average(List.of())).isNull();
    }

    @Test
    void checkinsByDayCountsEachGoalOnce() {
        List<ProgressRecord> records = List.of(
                record(1L, "2026-11-18"),
                record(1L, "2026-11-18"),
                record(2L, "2026-11-18"));
        assertThat(WellbeingStats.checkinsByDay(records))
                .containsEntry(LocalDate.parse("2026-11-18"), 2);
    }

    @Test
    void checkinsPerDaySplitsBySleep() {
        List<DayLog> logs = List.of(
                log("2026-11-16", 8),
                log("2026-11-17", 6),
                log("2026-11-18", 9));
        Map<LocalDate, Integer> checkins = Map.of(
                LocalDate.parse("2026-11-16"), 3,
                LocalDate.parse("2026-11-17"), 1,
                LocalDate.parse("2026-11-18"), 1);

        assertThat(WellbeingStats.checkinsPerDay(logs, checkins, true)).isEqualTo(2.0);
        assertThat(WellbeingStats.checkinsPerDay(logs, checkins, false)).isEqualTo(1.0);
    }
}
