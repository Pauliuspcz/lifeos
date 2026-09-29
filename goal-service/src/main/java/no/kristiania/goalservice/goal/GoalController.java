package no.kristiania.goalservice.goal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/goal")
public class GoalController {
    private final GoalService goalService;
    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<Goal> createGoal(@RequestBody Goal goal) {
        return ResponseEntity.ok(goalService.save(goal));
    }
    @GetMapping("/{id}")
    public ResponseEntity<Optional<Goal>> getGoalById(@PathVariable Long id) {
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

}
