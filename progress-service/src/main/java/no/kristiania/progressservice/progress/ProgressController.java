package no.kristiania.progressservice.progress;
import jakarta.validation.Valid;
import no.kristiania.progressservice.progress.dto.CreateProgressRequest;
import no.kristiania.progressservice.progress.dto.ProgressResponse;
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
    public ResponseEntity<ProgressResponse> create(@AuthenticationPrincipal Long userId, @Valid @RequestBody CreateProgressRequest request) {
        Progress saved = progressService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProgressResponse.from(saved));
    }

    @GetMapping
    public ResponseEntity<List<ProgressResponse>> findAll(@AuthenticationPrincipal Long userId, @RequestParam (required = false) Long goalId) {
        List<Progress> entries = progressService.findAll(userId, goalId);
        return ResponseEntity.ok(entries.stream().map(ProgressResponse::from).toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        progressService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
