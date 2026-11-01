package no.kristiania.reportservice.report;

import no.kristiania.reportservice.report.dto.GoalReport;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAdjusters;
import java.util.List;


@Service
public class ReportService {
    private final ProgressRecordRepo progressRecordRepo;
    public ReportService(ProgressRecordRepo progressRecordRepo) {
        this.progressRecordRepo = progressRecordRepo;
    }

    public GoalReport getGoalReport(Long userId, Long goalId){
        List<ProgressRecord> entries =
                progressRecordRepo.findByUserIdAndGoalIdOrderByDateAsc(userId,goalId);
        LocalDate lastDate = entries.isEmpty() ? null : entries.getLast().getDate();
        LocalDate today = LocalDate.now();
        LocalDate last30Start = today.minusDays(29);
        LocalDate previous30Start = today.minusDays(59);

        int last30Days = 0;
        int previous30Days = 0;
        for (ProgressRecord entry : entries) {
            LocalDate date = entry.getDate();
            if (!date.isBefore(last30Start)) {
                last30Days++;
            } else if (!date.isBefore(previous30Start)) {
                previous30Days++;
            }
        }

        ProgressRecord latest = entries.isEmpty() ? null : entries.getLast();
        String period = (latest == null || latest.getPeriod() == null) ? "DAILY" : latest.getPeriod();
        int target = (latest == null || latest.getTargetCount() == null) ? 1 : latest.getTargetCount();

        Period unit = switch (period) {
            case "WEEKLY" -> Period.ofWeeks(1);
            case "MONTHLY" -> Period.ofMonths(1);
            case "YEARLY" -> Period.ofYears(1);
            default -> Period.ofDays(1);
        };
        LocalDate start = switch (period) {
            case "WEEKLY" -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case "MONTHLY" -> today.withDayOfMonth(1);
            case "YEARLY" -> today.withDayOfYear(1);
            default -> today;
        };

        if (countIn(entries, start, unit) < target) start = start.minus(unit);
        int currentStreak = 0;
        while (countIn(entries, start, unit) >= target) {
            currentStreak++;
            start = start.minus(unit);
        }
        return new GoalReport(goalId, entries.size(), lastDate, last30Days, previous30Days, currentStreak);
    }

    private int countIn(List<ProgressRecord> entries, LocalDate start, Period unit) {
        LocalDate end = start.plus(unit);
        int count = 0;
        for (ProgressRecord entry : entries) {
            if (!entry.getDate().isBefore(start) && entry.getDate().isBefore(end)) count++;
        }
        return count;
    }

}
