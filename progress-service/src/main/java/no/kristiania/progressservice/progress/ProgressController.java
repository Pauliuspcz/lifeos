package no.kristiania.progressservice.progress;
import jakarta.validation.Valid;
import no.kristiania.progressservice.progress.dto.CreateProgressRequest;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Progress> create(@Valid @RequestBody CreateProgressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(progressService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<Progress>> findAll(@RequestParam (required = false) Long goalId) {
        return ResponseEntity.ok(progressService.findAll(goalId)); //to show session for goal
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        progressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
