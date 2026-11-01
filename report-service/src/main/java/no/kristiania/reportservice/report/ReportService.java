package no.kristiania.reportservice.report;

import no.kristiania.reportservice.report.dto.GoalReport;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Set<LocalDate> dates = new HashSet<>();
        for (ProgressRecord entry : entries) {
            dates.add(entry.getDate());
        }

        LocalDate day = dates.contains(today) ? today : today.minusDays(1);
        int currentStreak = 0;
        while (dates.contains(day)) {
            currentStreak++;
            day = day.minusDays(1);
        }

        return new GoalReport(goalId, entries.size(), lastDate, last30Days, previous30Days, currentStreak);
    }
}
