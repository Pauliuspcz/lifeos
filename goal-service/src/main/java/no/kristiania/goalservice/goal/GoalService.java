package no.kristiania.goalservice.goal;

import no.kristiania.goalservice.goal.dto.CreateGoalRequest;
import no.kristiania.goalservice.goal.dto.UpdateGoalRequest;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import no.kristiania.goalservice.goal.enums.GoalStatus;
import no.kristiania.goalservice.goal.exceptions.GoalArchivedException;
import no.kristiania.goalservice.goal.exceptions.GoalNotArchivedException;
import no.kristiania.goalservice.goal.exceptions.GoalNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GoalService {

    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    @Transactional
    public Goal create(Long userId, CreateGoalRequest request) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        applyFields(goal, request.title(), request.description(), request.period(),
                request.timesPerPeriod(), request.amountPerSession(), request.unit());
        return goalRepository.save(goal);
    }

    public List<Goal> findAll(Long userId, GoalStatus status) {
        return status == null
                ? goalRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                : goalRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
    }

    public Goal findById(Long userId, Long id) {
        return goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new GoalNotFoundException(id));
    }

    @Transactional
    public Goal update(Long userId, Long id, UpdateGoalRequest request) {
        Goal goal = findById(userId, id);
        if (goal.getStatus() == GoalStatus.ARCHIVED) {
            throw new GoalArchivedException();
        }
        applyFields(goal, request.title(), request.description(), request.period(),
                request.timesPerPeriod(), request.amountPerSession(), request.unit());
        return goalRepository.saveAndFlush(goal);
    }

    @Transactional
    public Goal archive(Long userId, Long id) {
        Goal goal = findById(userId, id);
        if (goal.getStatus() == GoalStatus.ACTIVE) {
            goal.setStatus(GoalStatus.ARCHIVED);
            goal.setArchivedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        }
        return goalRepository.saveAndFlush(goal);
    }

    @Transactional
    public Goal unarchive(Long userId, Long id) {
        Goal goal = findById(userId, id);
        if (goal.getStatus() == GoalStatus.ARCHIVED) {
            goal.setStatus(GoalStatus.ACTIVE);
            goal.setArchivedAt(null);
        }
        return goalRepository.saveAndFlush(goal);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Goal goal = findById(userId, id);
        if (goal.getStatus() != GoalStatus.ARCHIVED) {
            throw new GoalNotArchivedException();
        }
        goalRepository.delete(goal);
    }

    private void applyFields(Goal goal, String title, String description, GoalPeriod period,
                             int timesPerPeriod, BigDecimal amountPerSession, String unit) {
        goal.setTitle(title);
        goal.setDescription(description);
        goal.setPeriod(period);
        goal.setTimesPerPeriod(timesPerPeriod);
        goal.setAmountPerSession(amountPerSession);
        goal.setUnit(unit);
    }
}




