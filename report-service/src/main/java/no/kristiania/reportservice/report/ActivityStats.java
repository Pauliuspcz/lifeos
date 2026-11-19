package no.kristiania.reportservice.report;

import no.kristiania.reportservice.report.dto.ActivityDay;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ActivityStats {
    private static final int MAX_LEVEL = 4;
    private ActivityStats() {}

    public static LocalDate start(LocalDate today, int weeks) {
        return PeriodStats.periodStart("WEEKLY", today).minusWeeks(weeks - 1);
    }

    public static List<ActivityDay> days(List<ProgressRecord> entries, LocalDate today, int weeks) {
        LocalDate start = start(today, weeks);

        Map<LocalDate, Integer> counts = new HashMap<>();
        for (ProgressRecord entry : entries) {
            counts.merge(entry.getDate(), 1, Integer::sum);
        }

        List<ActivityDay> days = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(today); date = date.plusDays(1)) {
            int count = counts.getOrDefault(date, 0);
            days.add(new ActivityDay(date, count, Math.min(count, MAX_LEVEL)));
        }
        return days;
    }

    public static int sumLast(List<ActivityDay> days, int n) {
        int sum = 0;
        for (int i = Math.max(0, days.size() - n); i < days.size(); i++) {
            sum += days.get(i).count();
        }
        return sum;
    }
}
