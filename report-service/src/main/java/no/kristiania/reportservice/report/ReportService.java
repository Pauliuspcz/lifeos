package no.kristiania.reportservice.report;

import no.kristiania.reportservice.report.dto.GoalReport;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {
    private static final Long TEMP_USER_ID = 1L;

    private final ProgressRecordRepo progressRecordRepo;
    public ReportService(ProgressRecordRepo progressRecordRepo) {
        this.progressRecordRepo = progressRecordRepo;
    }

    public GoalReport getGoalReport(Long goalId){
        List<ProgressRecord> entries =
                progressRecordRepo.findByUserIdAndGoalIdOrderByDateAsc(TEMP_USER_ID,goalId);
        LocalDate lastDate = entries.isEmpty() ? null : entries.getLast().getDate();
        return new GoalReport(goalId, entries.size(), lastDate);
    }
}
