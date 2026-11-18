package no.kristiania.reportservice.report;

import no.kristiania.reportservice.report.dto.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class ReportService {
    private static final int HISTORY_SIZE = 12;
    private static final int ACTIVITY_WEEKS = 53;
    private final ProgressRecordRepo progressRecordRepo;
    public ReportService(ProgressRecordRepo progressRecordRepo) {
        this.progressRecordRepo = progressRecordRepo;
    }

    public GoalReport getGoalReport(Long userId, Long goalId){
        List<ProgressRecord> entries = onePerGoalDay(
                progressRecordRepo.findByUserIdAndGoalIdOrderByDateAsc(userId, goalId));
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
        int currentStreak = PeriodStats.currentStreak(entries, today);
        int successRate = PeriodStats.successRate(entries, today);
        List<PeriodCount> history = PeriodStats.history(entries, today, HISTORY_SIZE);
        Suggestion suggestion = PeriodStats.suggestion(entries, today);
        return new GoalReport(goalId, entries.size(), lastDate, last30Days, previous30Days, currentStreak, successRate, history, suggestion);
    }

    public ActivityReport getActivity(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate from = ActivityStats.start(today, ACTIVITY_WEEKS);
        List<ProgressRecord> entries = onePerGoalDay(
                progressRecordRepo.findByUserIdAndDateGreaterThanEqual(userId, from));
        List<ActivityDay> days = ActivityStats.days(entries, today, ACTIVITY_WEEKS);

        int totalCheckins = 0;
        int activeDays = 0;
        for (ActivityDay day : days) {
            totalCheckins += day.count();
            if (day.count() > 0) activeDays++;
        }
        return new ActivityReport(totalCheckins, activeDays, days);
    }

    private static List<ProgressRecord> onePerGoalDay(List<ProgressRecord> entries) {
        Map<String, ProgressRecord> seen = new LinkedHashMap<>();
        for (ProgressRecord e : entries) {
            seen.putIfAbsent(e.getGoalId() + "|" + e.getDate(), e);
        }
        return new ArrayList<>(seen.values());
    }
}
