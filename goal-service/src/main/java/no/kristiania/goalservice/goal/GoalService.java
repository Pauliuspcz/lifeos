package no.kristiania.goalservice.goal;
import no.kristiania.goalservice.goal.dto.CreateGoalRequest;
import no.kristiania.goalservice.goal.enums.GoalStatus;
import no.kristiania.goalservice.goal.exceptions.GoalNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GoalService {
    private final GoalRepo goalRepo;
    public GoalService(GoalRepo goalRepo) {
        this.goalRepo = goalRepo;
    }

    public Goal create(CreateGoalRequest request) {
        Goal goal = new Goal();
        goal.setTitle(request.title());
        goal.setDescription(request.description());
        goal.setPeriod(request.period());
        goal.setTimesPerPeriod(request.timesPerPeriod());
        goal.setAmountPerSession(request.amountPerSession());
        return goalRepo.save(goal);
    }
    public List<Goal> findAll() {
        return goalRepo.findAll();
    }
    public Goal findById(Long id) {
        return goalRepo.findById(id).orElseThrow(() -> new GoalNotFoundException(id));
    }
    public void deleteById(Long id) {
        goalRepo.deleteById(id);
    }
    public Goal archive(Long goalId) {
        Goal goal = findById(goalId);
        goal.setStatus(GoalStatus.ARCHIVED);
        return goalRepo.save(goal);
    }
}
