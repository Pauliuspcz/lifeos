package no.kristiania.progressservice.progress;
import jakarta.validation.Valid;
import no.kristiania.progressservice.progress.dto.CreateProgressRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<Progress> create(@AuthenticationPrincipal Long userId, @Valid @RequestBody CreateProgressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(progressService.create(userId,
                request));
    }

    @GetMapping
    public ResponseEntity<List<Progress>> findAll(@AuthenticationPrincipal Long userId, @RequestParam (required = false) Long goalId) {
        return ResponseEntity.ok(progressService.findAll(userId,
                goalId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        progressService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
