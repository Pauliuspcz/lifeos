package no.kristiania.reportservice.report;

import no.kristiania.reportservice.report.dto.GoalReport;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
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
        int currentStreak = StreakCalculator.currentStreak(entries, today);
        return new GoalReport(goalId, entries.size(), lastDate, last30Days, previous30Days, currentStreak);
    }
}
