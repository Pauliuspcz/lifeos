package no.kristiania.reportservice.daylog.controller;

import no.kristiania.reportservice.daylog.dto.DayLogRequest;
import no.kristiania.reportservice.daylog.service.DayLogService;
import no.kristiania.reportservice.daylog.dto.DayLogView;
import no.kristiania.reportservice.daylog.dto.Wellbeing;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports/day-log")
public class DayLogController {
    private final DayLogService dayLogService;

    public DayLogController(DayLogService dayLogService) {
        this.dayLogService = dayLogService;
    }

    @PutMapping
    public DayLogView save(@AuthenticationPrincipal Long userId, @RequestBody DayLogRequest request) {
        return dayLogService.saveToday(userId, request);
    }

    @GetMapping("/today")
    public DayLogView today(@AuthenticationPrincipal Long userId) {
        return dayLogService.today(userId);
    }
    @GetMapping("/summary")
    public Wellbeing summary(@AuthenticationPrincipal Long userId) {
        return dayLogService.summary(userId);
    }
}
