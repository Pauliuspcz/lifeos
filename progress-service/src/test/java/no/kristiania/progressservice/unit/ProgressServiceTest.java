package no.kristiania.progressservice.unit;

import no.kristiania.progressservice.client.GoalClient;
import no.kristiania.progressservice.client.GoalSummary;
import no.kristiania.progressservice.progress.Progress;
import no.kristiania.progressservice.progress.ProgressRepo;
import no.kristiania.progressservice.progress.ProgressService;
import no.kristiania.progressservice.progress.dto.CreateProgressRequest;
import no.kristiania.progressservice.progress.dto.DateRange;
import no.kristiania.progressservice.progress.dto.GoalProgressSummary;
import no.kristiania.progressservice.progress.exceptions.LogDateOutsideWindowException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {
    private static final Long USER_ID = 7L;
    private static final Long GOAL_ID = 11L;
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 20);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-20T12:00:00Z"), ZoneId.of("Europe/Oslo"));

    @Mock
    ProgressRepo repo;
    @Mock
    RabbitTemplate rabbitTemplate;
    @Mock
    GoalClient goalClient;

    ProgressService service;

    @BeforeEach
    void setUp() {
        service = new ProgressService(repo, rabbitTemplate, goalClient, CLOCK);
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
}
