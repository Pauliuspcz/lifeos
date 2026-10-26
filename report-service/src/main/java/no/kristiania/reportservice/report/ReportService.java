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
        return new GoalReport(goalId, entries.size(), lastDate);
    }
}
