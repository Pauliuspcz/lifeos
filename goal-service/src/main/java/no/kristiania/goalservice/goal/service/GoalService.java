package no.kristiania.goalservice.goal.service;

import lombok.extern.slf4j.Slf4j;
import no.kristiania.goalservice.catalog.entity.GoalTemplate;
import no.kristiania.goalservice.catalog.repository.GoalTemplateRepository;
import no.kristiania.goalservice.catalog.repository.CategoryRepository;
import no.kristiania.goalservice.goal.entity.Goal;
import no.kristiania.goalservice.goal.repository.GoalRepository;
import no.kristiania.goalservice.goal.dto.CreateGoalRequest;
import no.kristiania.goalservice.goal.dto.UpdateGoalRequest;
import no.kristiania.goalservice.shared.enums.GoalPeriod;
import no.kristiania.goalservice.goal.entity.GoalStatus;
import no.kristiania.goalservice.shared.enums.TrackingType;
import no.kristiania.goalservice.goal.events.GoalOutdoorChangedEvent;
import no.kristiania.goalservice.exception.GoalArchivedException;
import no.kristiania.goalservice.exception.GoalNotArchivedException;
import no.kristiania.goalservice.exception.GoalNotFoundException;
import no.kristiania.goalservice.exception.InvalidGoalException;
import org.springframework.context.ApplicationEventPublisher;
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

@Slf4j
@Service
@Transactional(readOnly = true)
public class GoalService {

    private final GoalRepository goalRepository;
    private final CategoryRepository categories;
    private final GoalTemplateRepository templates;
    private final ApplicationEventPublisher eventPublisher;

    public GoalService(GoalRepository goalRepository, CategoryRepository categories,
                       GoalTemplateRepository templates, ApplicationEventPublisher eventPublisher) {
        this.goalRepository = goalRepository;
        this.categories = categories;
        this.templates = templates;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Goal create(Long userId, CreateGoalRequest r) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setTrackingType(r.trackingType() != null ? r.trackingType() : TrackingType.CHECK);
        goal.setCategoryId(r.categoryId());
        goal.setTemplateId(r.templateId());
        goal.setOutdoor(Boolean.TRUE.equals(r.outdoor()));
        applyEditable(goal, new Editable(r.title(), r.description(), r.period(), r.timesPerPeriod(),
                r.targetAmount(), r.unit(), r.targetTime(), r.startDate(), r.targetDate(),
                r.motivation(), r.iconKey(), r.color()));
        validateReferences(goal);
        if (goal.getStartDate() == null) {
            goal.setStartDate(LocalDate.now());
        }
        validateAndNormalize(goal, true);
        Goal saved = goalRepository.save(goal);
        log.info("Goal created goalId={} userId={} trackingType={} outdoor={}",
                saved.getId(), userId, saved.getTrackingType(), saved.isOutdoor());
        if (goal.isOutdoor()) {
            eventPublisher.publishEvent(GoalOutdoorChangedEvent.of(goal, true));
        }
        return saved;
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
        LocalDate previousStartDate = goal.getStartDate();
        boolean wasOutdoor = goal.isOutdoor();
        applyEditable(goal, new Editable(r.title(), r.description(), r.period(), r.timesPerPeriod(),
                r.targetAmount(), r.unit(), r.targetTime(), r.startDate(), r.targetDate(),
                r.motivation(), r.iconKey(), r.color()));
        if (goal.getStartDate() == null) {
            goal.setStartDate(previousStartDate);
        }
        if (r.outdoor() != null) {
            goal.setOutdoor(r.outdoor());
        }
        boolean targetDateChanged = !Objects.equals(previousTargetDate, goal.getTargetDate());
        validateAndNormalize(goal, targetDateChanged);
        if (goal.isOutdoor() != wasOutdoor) {
            eventPublisher.publishEvent(GoalOutdoorChangedEvent.of(goal, goal.isOutdoor()));
        }
        Goal saved = goalRepository.saveAndFlush(goal);
        log.info("Goal updated goalId={} userId={}", id, userId);
        return saved;
    }

    @Transactional
    public Goal archive(Long userId, Long id) {
        Goal goal = findById(userId, id);
        boolean changed = false;
        if (goal.getStatus() == GoalStatus.ACTIVE) {
            goal.setStatus(GoalStatus.ARCHIVED);
            goal.setArchivedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
            if (goal.isOutdoor()) {
                eventPublisher.publishEvent(GoalOutdoorChangedEvent.of(goal, false));
            }
            changed = true;
        }
        Goal saved = goalRepository.saveAndFlush(goal);
        if (changed) {
            log.info("Goal archived goalId={} userId={}", id, userId);
        }
        return saved;
    }

    @Transactional
    public Goal unarchive(Long userId, Long id) {
        Goal goal = findById(userId, id);
        boolean changed = false;
        if (goal.getStatus() == GoalStatus.ARCHIVED) {
            goal.setStatus(GoalStatus.ACTIVE);
            goal.setArchivedAt(null);
            if (goal.isOutdoor()) {
                eventPublisher.publishEvent(GoalOutdoorChangedEvent.of(goal, true));
            }
            changed = true;
        }
        Goal saved = goalRepository.saveAndFlush(goal);
        if (changed) {
            log.info("Goal unarchived goalId={} userId={}", id, userId);
        }
        return saved;
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Goal goal = findById(userId, id);
        if (goal.getStatus() != GoalStatus.ARCHIVED) {
            throw new GoalNotArchivedException();
        }
        if (goal.isOutdoor()) {
            eventPublisher.publishEvent(GoalOutdoorChangedEvent.of(goal, false));
        }
        goalRepository.delete(goal);
        log.info("Goal deleted goalId={} userId={}", id, userId);
    }

    private record Editable(String title, String description, GoalPeriod period, Integer timesPerPeriod,
                            BigDecimal targetAmount, String unit, LocalTime targetTime,
                            LocalDate startDate, LocalDate targetDate, String motivation,
                            String iconKey, String color) {}

    private void applyEditable(Goal g, Editable e) {
        g.setTitle(e.title());
        g.setDescription(e.description());
        g.setPeriod(e.period());
        g.setTimesPerPeriod(e.timesPerPeriod());
        g.setTargetAmount(e.targetAmount());
        g.setUnit(e.unit());
        g.setTargetTime(e.targetTime());
        g.setStartDate(e.startDate());
        g.setTargetDate(e.targetDate());
        g.setMotivation(e.motivation());
        g.setIconKey(e.iconKey());
        g.setColor(e.color());
    }

    private void validateReferences(Goal g) {
        if (g.getCategoryId() != null && !categories.existsById(g.getCategoryId())) {
            throw new InvalidGoalException("Unknown categoryId " + g.getCategoryId());
        }
        if (g.getTemplateId() != null) {
            GoalTemplate t = templates.findById(g.getTemplateId())
                    .orElseThrow(() -> new InvalidGoalException("Unknown templateId " + g.getTemplateId()));
            if (g.getCategoryId() == null) {
                g.setCategoryId(t.getCategoryId());
            } else if (!g.getCategoryId().equals(t.getCategoryId())) {
                throw new InvalidGoalException("categoryId does not match the template's category");
            }
        }
    }

    private void validateAndNormalize(Goal g, boolean checkTargetDateInFuture) {
        switch (g.getTrackingType()) {
            case CHECK -> {
                if (g.getPeriod() == null) g.setPeriod(GoalPeriod.DAILY);
                if (g.getTimesPerPeriod() == null) g.setTimesPerPeriod(1);
            }
            case AMOUNT -> {
                require(g.getTargetAmount() != null, "AMOUNT requires targetAmount");
                require(g.getUnit() != null && !g.getUnit().isBlank(), "AMOUNT requires unit");
                require(g.getPeriod() != null || g.getTargetDate() != null,
                        "AMOUNT requires a period or a targetDate");
                require(g.getPeriod() != GoalPeriod.DAILY, "AMOUNT goals cannot be DAILY");
                g.setTimesPerPeriod(null);
            }
        }
        if (g.getTargetDate() != null && checkTargetDateInFuture) {
            require(g.getTargetDate().isAfter(LocalDate.now()), "targetDate must be in the future");
        }
        if (g.getStartDate() != null && g.getTargetDate() != null
                && g.getTargetDate().isBefore(g.getStartDate())) {
            throw new InvalidGoalException("targetDate cannot be before startDate");
        }
    }

    private void require(boolean ok, String message) {
        if (!ok) throw new InvalidGoalException(message);
    }
}