package no.kristiania.reportservice.report.daylog;

import java.util.List;

public final class WellbeingStats {
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
}
