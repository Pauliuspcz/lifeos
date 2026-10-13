package no.kristiania.progressservice.progress;

import no.kristiania.progressservice.progress.dto.CreateProgressRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {
    private final ProgressService progressService;
    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping
    public ResponseEntity<Progress> createGoal(@RequestBody CreateProgressRequest request) {
        return ResponseEntity.ok(progressService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<Progress>> findAll(@RequestParam (required = false) Long goalId) {
        return ResponseEntity.ok(progressService.findAll(goalId)); //to show session for goal
    }
}
