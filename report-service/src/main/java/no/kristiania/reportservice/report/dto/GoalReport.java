package no.kristiania.reportservice.report.dto;
import java.time.LocalDate;
import java.util.List;

public record GoalReport(
        Long goalId,
        int totalSessions,
        LocalDate lastDate,
        int last30Days,
        int previous30Days,
        int currentStreak,
        int successRate,
        List<PeriodCount> history
) {
}
