package no.kristiania.progressservice.progress;

import no.kristiania.progressservice.progress.dto.CreateProgressRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
