package no.kristiania.reportservice.unit.report;

import no.kristiania.reportservice.report.ActivityStats;
import no.kristiania.reportservice.report.ProgressRecord;
import no.kristiania.reportservice.report.dto.ActivityDay;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityStatsTest {
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 10, 28);

    private static ProgressRecord entry(String date) {
        ProgressRecord r = new ProgressRecord();
        r.setDate(LocalDate.parse(date));
        return r;
    }

    private static ActivityDay day(String date, int count, int level) {
        return new ActivityDay(LocalDate.parse(date), count, level);
    }

    @Test
    void startsOnMondayAndEndsToday() {
        List<ActivityDay> days = ActivityStats.days(List.of(), WEDNESDAY, 2);
        assertThat(days.getFirst().date()).isEqualTo(LocalDate.parse("2026-10-19"));
        assertThat(days.getLast().date()).isEqualTo(WEDNESDAY);
        assertThat(days).hasSize(10);
    }

    @Test
    void countsCheckinsPerDay() {
        List<ProgressRecord> entries = List.of(
                entry("2026-10-27"),
                entry("2026-10-27"),
                entry("2026-10-28"));
        List<ActivityDay> days = ActivityStats.days(entries, WEDNESDAY, 2);
        assertThat(days).contains(day("2026-10-27", 2, 2), day("2026-10-28", 1, 1));
    }

    @Test
    void emptyDaysAreZero() {
        List<ActivityDay> days = ActivityStats.days(List.of(entry("2026-10-27")), WEDNESDAY, 2);
        assertThat(days).contains(day("2026-10-26", 0, 0));
    }

    @Test
    void ignoresEntriesBeforeStart() {
        List<ActivityDay> days = ActivityStats.days(List.of(entry("2026-01-01")), WEDNESDAY, 2);
        assertThat(days).allMatch(d -> d.count() == 0);
    }

    @Test
    void levelStopsAtFour() {
        List<ProgressRecord> entries = Collections.nCopies(6, entry("2026-10-28"));
        List<ActivityDay> days = ActivityStats.days(entries, WEDNESDAY, 2);
        assertThat(days).contains(day("2026-10-28", 6, 4));
    }
}
