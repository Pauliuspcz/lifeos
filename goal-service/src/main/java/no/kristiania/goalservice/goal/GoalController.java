package no.kristiania.goalservice.goal;
import no.kristiania.goalservice.goal.dto.CreateGoalRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/goal")
public class GoalController {
    private final GoalService goalService;
    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<Goal> createGoal(@RequestBody CreateGoalRequest request) {
        return ResponseEntity.ok(goalService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Goal> getGoalById(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.findById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Goal>> getAllGoals() {
        return ResponseEntity.ok(goalService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGoalById(@PathVariable Long id) {
        goalService.deleteById(id);
        return ResponseEntity.ok("Goal with id: " + id + " has been deleted");
    }
    @PatchMapping("/{id}/archive")
    public ResponseEntity<Goal> archiveGoal(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.archive(id));
    }

}
