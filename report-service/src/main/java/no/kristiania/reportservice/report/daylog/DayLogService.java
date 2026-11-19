package no.kristiania.reportservice.report.daylog;

import no.kristiania.reportservice.report.dto.DayLogRequest;
import no.kristiania.reportservice.report.dto.DayLogView;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
public class DayLogService {
    private final DayLogRepo dayLogRepo;

    public DayLogService(DayLogRepo dayLogRepo) {
        this.dayLogRepo = dayLogRepo;
    }

    public DayLogView saveToday(Long userId, DayLogRequest request) {
        if (request.mood() != null && (request.mood() < 1 || request.mood() > 5)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mood must be between 1 and 5");
        }
        if (request.sleepHours() != null && (request.sleepHours() < 0 || request.sleepHours() > 24)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sleepHours must be between 0 and 24");
        }
        LocalDate today = LocalDate.now();
        DayLog log = dayLogRepo.findByUserIdAndDate(userId, today).orElseGet(() -> {
            DayLog created = new DayLog();
            created.setUserId(userId);
            created.setDate(today);
            return created;
        });
        if (request.mood() != null) log.setMood(request.mood());
        if (request.sleepHours() != null) log.setSleepHours(request.sleepHours());
        return DayLogView.from(dayLogRepo.save(log));
    }

    public DayLogView today(Long userId) {
        LocalDate today = LocalDate.now();
        return dayLogRepo.findByUserIdAndDate(userId, today)
                .map(DayLogView::from)
                .orElse(new DayLogView(today, null, null));
    }
}