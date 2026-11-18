package no.kristiania.progressservice.progress;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.progressservice.client.GoalClient;
import no.kristiania.progressservice.client.GoalSummary;
import no.kristiania.progressservice.client.TrackingModel;
import no.kristiania.progressservice.config.RabbitConfig;
import no.kristiania.progressservice.progress.dto.CreateProgressRequest;
import no.kristiania.progressservice.progress.dto.DateRange;
import no.kristiania.progressservice.progress.dto.GoalProgressSummary;
import no.kristiania.progressservice.progress.dto.ProgressEntryView;
import no.kristiania.progressservice.progress.dto.ProgressWindowResponse;
import no.kristiania.progressservice.progress.events.ProgressCreatedEvent;
import no.kristiania.progressservice.progress.events.ProgressDeletedEvent;
import no.kristiania.progressservice.progress.exceptions.*;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressRepo progressRepo;
    private final RabbitTemplate rabbitTemplate;
    private final GoalClient goalClient;
    private final Clock clock;

    private GoalSummary fetchGoal(Long goalId) {
        try {
            return goalClient.getGoal(goalId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new GoalNotFoundException(goalId);
        } catch (ResourceAccessException | HttpServerErrorException e) {
            log.error("goal-service unavailable", e);
            throw new GoalUnavailableException();
        }
    }

    public Progress create(Long userId, CreateProgressRequest request) {
        GoalSummary goal = fetchGoal(request.goalId());
        if (!"ACTIVE".equals(goal.status())) {
            throw new GoalNotActiveException(goal.id());
        }
        TrackingModel model = TrackingModel.from(goal);
        boolean cumulative = model == TrackingModel.CUMULATIVE_SUM;
        if (cumulative && request.amount() == null) {
            throw new AmountRequiredException();
        }

        LocalDate today = LocalDate.now(clock);
        if (!LogWindow.contains(model, today, request.date())) {
            throw new LogDateOutsideWindowException(LogWindow.earliest(model, today), today);
        }

        if (cumulative && goal.period() != null) {
            LocalDate periodStart = AmountPeriod.current(goal.period(), goal.startDate(), today).from();
            if (request.date().isBefore(periodStart)) {
                throw new LogDateOutsideWindowException(periodStart, today);
            }
        }

        if (!cumulative) {
            Optional<Progress> existing = progressRepo
                    .findByUserIdAndGoalIdAndDate(userId, goal.id(), request.date());
            if (existing.isPresent()) return existing.get();
        }

        Progress progress = new Progress();
        progress.setGoalId(goal.id());
        progress.setDate(request.date());
        progress.setUserId(userId);
        progress.setTrackingModel(model);
        progress.setAmount(cumulative ? request.amount() : null);

        boolean counted = model == TrackingModel.WEEKLY_COUNT || model == TrackingModel.MONTHLY_COUNT;
        int targetCount = (counted && goal.timesPerPeriod() != null) ? goal.timesPerPeriod() : 1;
        // Amount goals count as "days with progress" in report-service
        String eventPeriod = cumulative ? "DAILY" : goal.period();

        Progress saved;
        try {
            saved = progressRepo.save(progress);
        } catch (DataIntegrityViolationException e) {
            return progressRepo.findByUserIdAndGoalIdAndDate(userId, goal.id(), request.date())
                    .orElseThrow(() -> e);
        }
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_KEY_CREATED,
                    ProgressCreatedEvent.from(saved, targetCount, eventPeriod));
        } catch (AmqpException e) {
            log.error("Could not publish ProgressCreatedEvent {}", saved.getEventId(), e);
        }
        return saved;
    }

    public List<Progress> findAll(Long userId, Long goalId) {
        if (goalId == null) {
            return progressRepo.findByUserIdOrderByDateDesc(userId);
        }
        return progressRepo.findByGoalIdAndUserIdOrderByDateDesc(goalId, userId);
    }

    public void delete(Long userId, Long id) {
        Progress progress = progressRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ProgressNotFoundException(id));
        LocalDate today = LocalDate.now(clock);
        if (!LogWindow.contains(progress.getTrackingModel(), today, progress.getDate())) {
            throw new LogDateOutsideWindowException(
                    LogWindow.earliest(progress.getTrackingModel(), today), today);
        }
        progressRepo.delete(progress);
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_KEY_DELETED, ProgressDeletedEvent.from(progress));
        } catch (AmqpException e) {
            log.error("Could not publish ProgressDeletedEvent for progress {}", progress.getId(), e);
        }
    }

    public ProgressWindowResponse window(Long userId, Long goalId) {
        LocalDate today = LocalDate.now(clock);
        LocalDate weekStart = LogWindow.weekStart(today);
        LocalDate monthStart = LogWindow.monthStart(today);
        LocalDate from = today.minusDays(6);
        LocalDate fetchFrom = monthStart.isBefore(from) ? monthStart : from;

        List<ProgressEntryView> entries = progressRepo
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, fetchFrom, today).stream()
                .filter(p -> goalId == null || p.getGoalId().equals(goalId))
                .map(p -> ProgressEntryView.from(p,
                        LogWindow.contains(p.getTrackingModel(), today, p.getDate())))
                .toList();

        Map<TrackingModel, DateRange> editable = new EnumMap<>(TrackingModel.class);
        for (TrackingModel m : TrackingModel.values()) {
            editable.put(m, new DateRange(LogWindow.earliest(m, today), today));
        }
        return new ProgressWindowResponse(today, weekStart, monthStart, from, editable, entries);
    }

    public GoalProgressSummary summary(Long userId, Long goalId) {
        GoalSummary goal = fetchGoal(goalId);
        if (TrackingModel.from(goal) != TrackingModel.CUMULATIVE_SUM) {
            throw new UnsupportedTrackingException("Goal is not an amount goal");
        }
        LocalDate today = LocalDate.now(clock);
        DateRange current = goal.period() == null ? null
                : AmountPeriod.current(goal.period(), goal.startDate(), today);

        BigDecimal total = current == null
                ? progressRepo.sumAmount(userId, goalId)
                : progressRepo.sumAmountBetween(userId, goalId, current.from(), current.to());
        LocalDate paceFrom = current == null ? goal.startDate() : current.from();
        LocalDate paceTo = current == null ? goal.targetDate() : current.to();
        BigDecimal expected = Pace.expected(goal.targetAmount(), paceFrom, paceTo, today);
        BigDecimal delta = expected == null ? null : total.subtract(expected);
        return new GoalProgressSummary(goalId, total, goal.targetAmount(), goal.unit(),
                goal.startDate(), goal.targetDate(), current, expected, delta);
    }
}