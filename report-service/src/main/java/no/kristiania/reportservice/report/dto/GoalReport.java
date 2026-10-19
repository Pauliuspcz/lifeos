package no.kristiania.reportservice.report.dto;
import java.time.LocalDate;

public record GoalReport(
        Long goalId,
        int totalSessions,
        LocalDate lastDate
) {
}
