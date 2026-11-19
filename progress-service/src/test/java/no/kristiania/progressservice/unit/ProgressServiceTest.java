package no.kristiania.progressservice.unit;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import no.kristiania.progressservice.client.GoalClient;
import no.kristiania.progressservice.client.GoalSummary;
import no.kristiania.progressservice.progress.entity.Progress;
import no.kristiania.progressservice.progress.repository.ProgressRepository;
import no.kristiania.progressservice.progress.service.ProgressService;
import no.kristiania.progressservice.progress.dto.CreateProgressRequest;
import no.kristiania.progressservice.progress.dto.DateRange;
import no.kristiania.progressservice.progress.dto.GoalProgressSummary;
import no.kristiania.progressservice.exceptions.AmountRequiredException;
import no.kristiania.progressservice.exceptions.LogDateOutsideWindowException;
import no.kristiania.progressservice.exceptions.PeriodTargetReachedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {
    private static final Long USER_ID = 7L;
    private static final Long GOAL_ID = 11L;
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 20);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-20T12:00:00Z"), ZoneId.of("Europe/Oslo"));

    @Mock
    ProgressRepository repo;
    @Mock
    RabbitTemplate rabbitTemplate;
    @Mock
    GoalClient goalClient;

    ProgressService service;

    @BeforeEach
    void setUp() {
        service = new ProgressService(repo, rabbitTemplate, goalClient, CircuitBreaker.ofDefaults("goal"), CLOCK);
    }

    private static GoalSummary monthlyGoal(LocalDate start) {
        return new GoalSummary(GOAL_ID, "ACTIVE", "AMOUNT", null, "MONTHLY",
                new BigDecimal("50"), "km", start, null);
    }

    private static GoalSummary dateGoal(LocalDate start) {
        return new GoalSummary(GOAL_ID, "ACTIVE", "AMOUNT", null, null,
                new BigDecimal("1000"), "km", start, LocalDate.of(2027, 6, 1));
    }

    private static CreateProgressRequest log(LocalDate date) {
        return new CreateProgressRequest(GOAL_ID, date, new BigDecimal("5"));
    }

    private static GoalSummary dailyGoal() {
        return new GoalSummary(GOAL_ID, "ACTIVE", "CHECK", 1, "DAILY", null, null, TODAY, null);
    }

    private static GoalSummary monthlyCheckGoal(int times) {
        return new GoalSummary(GOAL_ID, "ACTIVE", "CHECK", times, "MONTHLY", null, null, TODAY, null);
    }

    @Test
    void createBeforePeriodStartThrows() {
        when(goalClient.getGoal(GOAL_ID)).thenReturn(monthlyGoal(TODAY));

        assertThatThrownBy(() -> service.create(USER_ID, log(TODAY.minusDays(1))))
                .isInstanceOf(LogDateOutsideWindowException.class);
        verify(repo, never()).save(any());
    }

    @Test
    void createInsidePeriodSaves() {
        when(goalClient.getGoal(GOAL_ID)).thenReturn(monthlyGoal(TODAY.minusDays(2)));
        when(repo.save(any(Progress.class))).thenAnswer(i -> i.getArgument(0));

        Progress saved = service.create(USER_ID, log(TODAY.minusDays(1)));

        assertThat(saved.getDate()).isEqualTo(TODAY.minusDays(1));
    }

    @Test
    void createOnDateGoalCanLogBeforeStart() {
        when(goalClient.getGoal(GOAL_ID)).thenReturn(dateGoal(TODAY));
        when(repo.save(any(Progress.class))).thenAnswer(i -> i.getArgument(0));

        Progress saved = service.create(USER_ID, log(TODAY.minusDays(2)));

        assertThat(saved.getDate()).isEqualTo(TODAY.minusDays(2));
    }

    @Test
    void summaryForMonthlyGoalSumsCurrentPeriod() {
        LocalDate periodStart = LocalDate.of(2026, 11, 18);
        LocalDate periodEnd = LocalDate.of(2026, 12, 17);
        when(goalClient.getGoal(GOAL_ID)).thenReturn(monthlyGoal(LocalDate.of(2026, 10, 18)));
        when(repo.sumAmountBetween(USER_ID, GOAL_ID, periodStart, periodEnd))
                .thenReturn(new BigDecimal("20"));

        GoalProgressSummary summary = service.summary(USER_ID, GOAL_ID);

        assertThat(summary.total()).isEqualByComparingTo("20");
        assertThat(summary.currentPeriod()).isEqualTo(new DateRange(periodStart, periodEnd));
    }

    @Test
    void summaryForDateGoalSumsAllTime() {
        when(goalClient.getGoal(GOAL_ID)).thenReturn(dateGoal(LocalDate.of(2026, 9, 1)));
        when(repo.sumAmount(USER_ID, GOAL_ID)).thenReturn(new BigDecimal("120"));

        GoalProgressSummary summary = service.summary(USER_ID, GOAL_ID);

        assertThat(summary.total()).isEqualByComparingTo("120");
        assertThat(summary.currentPeriod()).isNull();
    }

    @Test
    void createAmountWithoutAmountThrows() {
        when(goalClient.getGoal(GOAL_ID)).thenReturn(monthlyGoal(TODAY));

        assertThatThrownBy(() -> service.create(USER_ID, new CreateProgressRequest(GOAL_ID, TODAY, null)))
                .isInstanceOf(AmountRequiredException.class);
        verify(repo, never()).save(any());
    }

    @Test
    void createSameDayTwiceReturnsExisting() {
        Progress existing = new Progress();
        when(goalClient.getGoal(GOAL_ID)).thenReturn(dailyGoal());
        when(repo.findByUserIdAndGoalIdAndDate(USER_ID, GOAL_ID, TODAY)).thenReturn(Optional.of(existing));

        Progress result = service.create(USER_ID, new CreateProgressRequest(GOAL_ID, TODAY, null));

        assertThat(result).isSameAs(existing);
        verify(repo, never()).save(any());
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void createWhenMonthlyTargetReachedThrows() {
        when(goalClient.getGoal(GOAL_ID)).thenReturn(monthlyCheckGoal(1));
        when(repo.countByUserIdAndGoalIdAndDateBetween(any(), any(), any(), any())).thenReturn(1L);

        assertThatThrownBy(() -> service.create(USER_ID, new CreateProgressRequest(GOAL_ID, TODAY, null)))
                .isInstanceOf(PeriodTargetReachedException.class);
        verify(repo, never()).save(any());
    }

    @Test
    void createBelowMonthlyTargetSaves() {
        when(goalClient.getGoal(GOAL_ID)).thenReturn(monthlyCheckGoal(3));
        when(repo.countByUserIdAndGoalIdAndDateBetween(any(), any(), any(), any())).thenReturn(2L);
        when(repo.save(any(Progress.class))).thenAnswer(i -> i.getArgument(0));

        Progress saved = service.create(USER_ID, new CreateProgressRequest(GOAL_ID, TODAY, null));

        assertThat(saved.getDate()).isEqualTo(TODAY);
    }
}
