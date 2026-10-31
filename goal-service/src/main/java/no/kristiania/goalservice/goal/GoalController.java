package no.kristiania.goalservice.goal;

import jakarta.validation.Valid;
import no.kristiania.goalservice.goal.dto.CreateGoalRequest;
import no.kristiania.goalservice.goal.dto.GoalResponse;
import no.kristiania.goalservice.goal.dto.UpdateGoalRequest;
import no.kristiania.goalservice.goal.enums.GoalStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/goal")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> create(@AuthenticationPrincipal Long userId,
                                               @Valid @RequestBody CreateGoalRequest request) {
        Goal goal = goalService.create(userId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(goal.getId()).toUri();
        return ResponseEntity.created(location).body(GoalResponse.from(goal));
    }

    @GetMapping
    public List<GoalResponse> list(@AuthenticationPrincipal Long userId,
                                   @RequestParam(required = false) GoalStatus status) {
        return goalService.findAll(userId, status).stream().map(GoalResponse::from).toList();
    }

    @GetMapping("/{id}")
    public GoalResponse get(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return GoalResponse.from(goalService.findById(userId, id));
    }

    @PutMapping("/{id}")
    public GoalResponse update(@AuthenticationPrincipal Long userId, @PathVariable Long id,
                               @Valid @RequestBody UpdateGoalRequest request) {
        return GoalResponse.from(goalService.update(userId, id, request));
    }

    @PatchMapping("/{id}/archive")
    public GoalResponse archive(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return GoalResponse.from(goalService.archive(userId, id));
    }

    @PatchMapping("/{id}/unarchive")
    public GoalResponse unarchive(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return GoalResponse.from(goalService.unarchive(userId, id));
    }

    //TODO fjerne?
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        goalService.delete(userId, id);
    }
}