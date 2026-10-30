package no.kristiania.goalservice.goal;

import no.kristiania.goalservice.goal.dto.CreateGoalRequest;
import no.kristiania.goalservice.goal.dto.UpdateGoalRequest;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import no.kristiania.goalservice.goal.enums.GoalStatus;
import no.kristiania.goalservice.goal.enums.GoalType;
import no.kristiania.goalservice.goal.enums.TrackingType;
import no.kristiania.goalservice.goal.exceptions.GoalArchivedException;
import no.kristiania.goalservice.goal.exceptions.GoalNotArchivedException;
import no.kristiania.goalservice.goal.exceptions.GoalNotFoundException;
import no.kristiania.goalservice.goal.exceptions.InvalidGoalException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class GoalService {

    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    @Transactional
    public Goal create(Long userId, CreateGoalRequest r) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setGoalType(r.goalType() != null ? r.goalType() : GoalType.HABIT);
        goal.setTrackingType(r.trackingType() != null ? r.trackingType() : TrackingType.COUNT);
        goal.setCategoryId(r.categoryId());
        goal.setTemplateId(r.templateId());
        applyEditable(goal, new Editable(r.title(), r.description(), r.period(), r.timesPerPeriod(),
                r.amountPerSession(), r.unit(), r.targetTime(), r.startDate(), r.targetDate(),
                r.motivation(), r.iconKey(), r.color()));
        validateAndNormalize(goal, true);
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
    public Goal update(Long userId, Long id, UpdateGoalRequest r) {
        Goal goal = findById(userId, id);
        if (goal.getStatus() == GoalStatus.ARCHIVED) {
            throw new GoalArchivedException();
        }
        if (r.version() != null && !r.version().equals(goal.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Goal.class, id);
        }
        LocalDate previousTargetDate = goal.getTargetDate();
        applyEditable(goal, new Editable(r.title(), r.description(), r.period(), r.timesPerPeriod(),
                r.amountPerSession(), r.unit(), r.targetTime(), r.startDate(), r.targetDate(),
                r.motivation(), r.iconKey(), r.color()));
        boolean targetDateChanged = !Objects.equals(previousTargetDate, goal.getTargetDate());
        validateAndNormalize(goal, targetDateChanged);
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

    private record Editable(String title, String description, GoalPeriod period, Integer timesPerPeriod,
                            BigDecimal amountPerSession, String unit, LocalTime targetTime,
                            LocalDate startDate, LocalDate targetDate, String motivation,
                            String iconKey, String color) {}

    private void applyEditable(Goal g, Editable e) {
        g.setTitle(e.title());
        g.setDescription(e.description());
        g.setPeriod(e.period());
        g.setTimesPerPeriod(e.timesPerPeriod());
        g.setAmountPerSession(e.amountPerSession());
        g.setUnit(e.unit());
        g.setTargetTime(e.targetTime());
        g.setStartDate(e.startDate());
        g.setTargetDate(e.targetDate());
        g.setMotivation(e.motivation());
        g.setIconKey(e.iconKey());
        g.setColor(e.color());
    }

    private void validateAndNormalize(Goal g, boolean checkTargetDateInFuture) {
        TrackingType t = g.getTrackingType();
        switch (t) {
            case COUNT -> require(g.getPeriod() != null && g.getTimesPerPeriod() != null,
                    "COUNT requires period and timesPerPeriod");
            case AMOUNT, DURATION, LIMIT -> require(
                    g.getAmountPerSession() != null && g.getUnit() != null && !g.getUnit().isBlank(),
                    t + " requires amountPerSession and unit");
            case TIME_TARGET -> require(g.getTargetTime() != null, "TIME_TARGET requires targetTime");
            case EVENT -> {
                require(g.getTargetDate() != null, "EVENT requires targetDate");
                if (checkTargetDateInFuture) {
                    require(g.getTargetDate().isAfter(LocalDate.now()), "targetDate must be in the future");
                }
            }
            case CHECK -> { }
        }
        if (g.getStartDate() != null && g.getTargetDate() != null
                && g.getTargetDate().isBefore(g.getStartDate())) {
            throw new InvalidGoalException("targetDate cannot be before startDate");
        }
        if (g.getPeriod() == null && t != TrackingType.EVENT) g.setPeriod(GoalPeriod.DAILY);
        if (g.getTimesPerPeriod() == null) g.setTimesPerPeriod(1);
    }

    private void require(boolean ok, String message) {
        if (!ok) throw new InvalidGoalException(message);
    }
}