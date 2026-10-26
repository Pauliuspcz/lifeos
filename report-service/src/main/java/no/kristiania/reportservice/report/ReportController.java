package no.kristiania.reportservice.report;

import no.kristiania.reportservice.report.dto.GoalReport;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/goals/{goalId}")
    public ResponseEntity<GoalReport> getGoalReport(@AuthenticationPrincipal Long userId, @PathVariable Long goalId){
        return ResponseEntity.ok(reportService.getGoalReport(userId, goalId));
    }

}
