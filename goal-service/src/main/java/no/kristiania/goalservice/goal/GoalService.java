package no.kristiania.goalservice.goal;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GoalService {
    private final GoalRepo goalRepo;

    public GoalService(GoalRepo goalRepo) {
        this.goalRepo = goalRepo;
    }
    public Goal save(Goal goal) {
        return goalRepo.save(goal);
    }
    public List<Goal> findAll() {
        return goalRepo.findAll();
    }
    public Optional<Goal> findById(Long id) {
        return goalRepo.findById(id);
    }
    public void deleteById(Long id) {
        goalRepo.deleteById(id);
    }
}
