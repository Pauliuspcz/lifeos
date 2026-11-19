package no.kristiania.progressservice.progress.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.progressservice.client.GoalClient;
import no.kristiania.progressservice.client.GoalSummary;
import no.kristiania.progressservice.config.RabbitConfig;
import no.kristiania.progressservice.exceptions.*;
import no.kristiania.progressservice.progress.dto.*;
import no.kristiania.progressservice.progress.entity.Progress;
import no.kristiania.progressservice.progress.entity.TrackingModel;
import no.kristiania.progressservice.progress.events.ProgressCreatedEvent;
import no.kristiania.progressservice.progress.events.ProgressDeletedEvent;
import no.kristiania.progressservice.progress.repository.ProgressRepository;
import no.kristiania.progressservice.progress.utils.AmountPeriod;
import no.kristiania.progressservice.progress.utils.LogWindow;
import no.kristiania.progressservice.progress.utils.Pace;
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

    private final ProgressRepository progressRepository;
    private final RabbitTemplate rabbitTemplate;
    private final GoalClient goalClient;
    private final CircuitBreaker goalCircuitBreaker;
    private final Clock clock;

    private GoalSummary fetchGoal(Long goalId) {
        try {
            return goalCircuitBreaker.executeSupplier(() -> goalClient.getGoal(goalId));
        } catch (CallNotPermittedException e) {
            log.warn("Goal circuit breaker open, failing fast goalId={}", goalId);
            throw new GoalUnavailableException();
        } catch (HttpClientErrorException.NotFound e) {
            throw new GoalNotFoundException(goalId);
        } catch (HttpClientErrorException e) {
            log.error("goal-service rejected the request status={} goalId={}",
                    e.getStatusCode().value(), goalId, e);
            throw new GoalUnavailableException();
        } catch (ResourceAccessException | HttpServerErrorException e) {
            log.warn("goal-service unavailable goalId={} type={}", goalId, e.getClass().getSimpleName());
            log.debug("goal-service call failed", e);
            throw new GoalUnavailableException();
        }
    }

    // Not @Transactional on purpose: the DataIntegrityViolationException catch below only works
    // when save() commits by itself.
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
            Optional<Progress> existing = progressRepository
                    .findByUserIdAndGoalIdAndDate(userId, goal.id(), request.date());
            if (existing.isPresent()) {
                log.debug("Progress already logged progressId={} userId={} goalId={}",
                        existing.get().getId(), userId, goal.id());
                return existing.get();
            }
        }

        boolean counted = model == TrackingModel.WEEKLY_COUNT || model == TrackingModel.MONTHLY_COUNT;
        int targetCount = (counted && goal.timesPerPeriod() != null) ? goal.timesPerPeriod() : 1;
        if (counted) {
            boolean weekly = model == TrackingModel.WEEKLY_COUNT;
            LocalDate from = weekly ? LogWindow.weekStart(request.date()) : LogWindow.monthStart(request.date());
            LocalDate to = weekly ? from.plusWeeks(1).minusDays(1) : from.plusMonths(1).minusDays(1);
            if (progressRepository.countByUserIdAndGoalIdAndDateBetween(userId, goal.id(), from, to) >= targetCount) {
                throw new PeriodTargetReachedException();
            }
        }

        Progress progress = new Progress();
        progress.setGoalId(goal.id());
        progress.setDate(request.date());
        progress.setUserId(userId);
        progress.setTrackingModel(model);
        progress.setAmount(cumulative ? request.amount() : null);

        // Amount goals count as "days with progress" in report-service
        String eventPeriod = cumulative ? "DAILY" : goal.period();

        Progress saved;
        try {
            saved = progressRepository.save(progress);
        } catch (DataIntegrityViolationException e) {
            log.debug("Duplicate progress ignored userId={} goalId={}", userId, goal.id());
            return progressRepository.findByUserIdAndGoalIdAndDate(userId, goal.id(), request.date())
                    .orElseThrow(() -> e);
        }
        log.info("Progress created progressId={} userId={} goalId={} trackingModel={}",
                saved.getId(), userId, goal.id(), model);
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_KEY_CREATED,
                    ProgressCreatedEvent.from(saved, targetCount, eventPeriod));
            log.debug("Progress created event published progressId={} eventId={}",
                    saved.getId(), saved.getEventId());
        } catch (AmqpException e) {
            log.error("Progress created event publish failed progressId={} eventId={}",
                    saved.getId(), saved.getEventId(), e);
        }
        return saved;
    }

    public List<Progress> findAll(Long userId, Long goalId) {
        if (goalId == null) {
            return progressRepository.findByUserIdOrderByDateDesc(userId);
        }
        return progressRepository.findByGoalIdAndUserIdOrderByDateDesc(goalId, userId);
    }

    public void delete(Long userId, Long id) {
        Progress progress = progressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ProgressNotFoundException(id));
        LocalDate today = LocalDate.now(clock);
        if (!LogWindow.contains(progress.getTrackingModel(), today, progress.getDate())) {
            throw new LogDateOutsideWindowException(
                    LogWindow.earliest(progress.getTrackingModel(), today), today);
        }
        progressRepository.delete(progress);
        log.info("Progress deleted progressId={} userId={} goalId={}",
                progress.getId(), userId, progress.getGoalId());
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_KEY_DELETED, ProgressDeletedEvent.from(progress));
        } catch (AmqpException e) {
            log.error("Progress deleted event publish failed progressId={}", progress.getId(), e);
        }
    }

    public ProgressWindowResponse window(Long userId, Long goalId) {
        LocalDate today = LocalDate.now(clock);
        LocalDate weekStart = LogWindow.weekStart(today);
        LocalDate monthStart = LogWindow.monthStart(today);
        LocalDate from = today.minusDays(6);
        LocalDate fetchFrom = monthStart.isBefore(from) ? monthStart : from;

        List<ProgressEntryView> entries = progressRepository
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
                ? progressRepository.sumAmount(userId, goalId)
                : progressRepository.sumAmountBetween(userId, goalId, current.from(), current.to());
        LocalDate paceFrom = current == null ? goal.startDate() : current.from();
        LocalDate paceTo = current == null ? goal.targetDate() : current.to();
        BigDecimal expected = Pace.expected(goal.targetAmount(), paceFrom, paceTo, today);
        BigDecimal delta = expected == null ? null : total.subtract(expected);
        return new GoalProgressSummary(goalId, total, goal.targetAmount(), goal.unit(),
                goal.startDate(), goal.targetDate(), current, expected, delta);
    }
}