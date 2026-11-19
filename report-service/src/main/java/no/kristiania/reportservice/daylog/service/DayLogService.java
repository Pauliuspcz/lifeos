package no.kristiania.reportservice.daylog.service;

import lombok.extern.slf4j.Slf4j;
import no.kristiania.reportservice.daylog.dto.DayLogRequest;
import no.kristiania.reportservice.daylog.dto.DayLogView;
import no.kristiania.reportservice.daylog.dto.Wellbeing;
import no.kristiania.reportservice.daylog.entity.DayLog;
import no.kristiania.reportservice.daylog.repository.DayLogRepository;
import no.kristiania.reportservice.daylog.utils.WellbeingStats;
import no.kristiania.reportservice.exceptions.InvalidDayLogException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class DayLogService {
    private static final int SUMMARY_DAYS = 30;
    private final DayLogRepository dayLogRepository;

    public DayLogService(DayLogRepository dayLogRepository) {
        this.dayLogRepository = dayLogRepository;
    }

    public DayLogView saveToday(Long userId, DayLogRequest request) {
        if (request.mood() != null && (request.mood() < 1 || request.mood() > 5)) {
            throw new InvalidDayLogException("mood must be between 1 and 5");
        }
        if (request.sleepHours() != null && (request.sleepHours() < 0 || request.sleepHours() > 24)) {
            throw new InvalidDayLogException("sleepHours must be between 0 and 24");
        }
        LocalDate today = LocalDate.now();
        DayLog dayLog = dayLogRepository.findByUserIdAndDate(userId, today).orElseGet(() -> {
            DayLog created = new DayLog();
            created.setUserId(userId);
            created.setDate(today);
            return created;
        });
        if (request.mood() != null) dayLog.setMood(request.mood());
        if (request.sleepHours() != null) dayLog.setSleepHours(request.sleepHours());
        DayLogView saved = DayLogView.from(dayLogRepository.save(dayLog));
        log.info("Day log saved userId={} moodSet={} sleepSet={}",
                userId, request.mood() != null, request.sleepHours() != null);
        return saved;
    }

    public DayLogView today(Long userId) {
        LocalDate today = LocalDate.now();
        return dayLogRepository.findByUserIdAndDate(userId, today)
                .map(DayLogView::from)
                .orElse(new DayLogView(today, null, null));
    }

    public Wellbeing summary(Long userId) {
        LocalDate from = LocalDate.now().minusDays(SUMMARY_DAYS - 1);
        List<DayLog> logs = dayLogRepository.findByUserIdAndDateGreaterThanEqual(userId, from);
        return new Wellbeing(
                WellbeingStats.average(logs.stream().map(DayLog::getMood).toList()),
                WellbeingStats.average(logs.stream().map(DayLog::getSleepHours).toList()),
                logs.size());
    }
}