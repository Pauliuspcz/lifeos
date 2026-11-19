package no.kristiania.reportservice.report.daylog;

import no.kristiania.reportservice.report.ProgressRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WellbeingStats {
    private static final int GOOD_SLEEP_HOURS = 8;
    private WellbeingStats() {}

    public static Double average(List<Integer> values) {
        int sum = 0;
        int count = 0;
        for (Integer value : values) {
            if (value == null) continue;
            sum += value;
            count++;
        }
        return count == 0 ? null : Math.round(sum * 10.0 / count) / 10.0;
    }

    public static Map<LocalDate, Integer> checkinsByDay(List<ProgressRecord> records) {
        Map<LocalDate, Set<Long>> goalsPerDay = new HashMap<>();
        for (ProgressRecord record : records) {
            goalsPerDay.computeIfAbsent(record.getDate(), d -> new HashSet<>()).add(record.getGoalId());
        }
        Map<LocalDate, Integer> counts = new HashMap<>();
        goalsPerDay.forEach((date, goals) -> counts.put(date, goals.size()));
        return counts;
    }

    public static Double checkinsPerDay(List<DayLog> logs, Map<LocalDate, Integer> checkins, boolean wellRested) {
        List<Integer> counts = new ArrayList<>();
        for (DayLog log : logs) {
            if (log.getSleepHours() == null) continue;
            if ((log.getSleepHours() >= GOOD_SLEEP_HOURS) == wellRested) {
                counts.add(checkins.getOrDefault(log.getDate(), 0));
            }
        }
        return average(counts);
    }
}
