package no.kristiania.goalservice.unit.goal;

import no.kristiania.goalservice.goal.Goal;
import no.kristiania.goalservice.goal.GoalRepository;
import no.kristiania.goalservice.goal.GoalService;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long GOAL_ID = 5L;

    @Mock
    private GoalRepository goalRepo;

    @InjectMocks
    private GoalService goalService;

    private Goal existingGoal() {
        Goal goal = new Goal();
        goal.setId(GOAL_ID);
        goal.setUserId(USER_ID);
        goal.setTitle("Run");
        goal.setPeriod(GoalPeriod.WEEKLY);
        goal.setTimesPerPeriod(3);
        return goal;
    }

    private CreateGoalRequest createRequest(TrackingType tracking, GoalPeriod period, Integer times,
                                            BigDecimal amount, String unit, LocalTime targetTime,
                                            LocalDate startDate, LocalDate targetDate) {
        return new CreateGoalRequest("Goal", null, null, tracking, period, times, amount, unit,
                targetTime, startDate, targetDate, null, null, null, null, null);
    }

    private UpdateGoalRequest updateRequest(String title, String description, GoalPeriod period,
                                            Integer times, BigDecimal amount, String unit) {
        return new UpdateGoalRequest(title, description, period, times, amount, unit,
                null, null, null, null, null, null, null);
    }

    private UpdateGoalRequest updateRequestWithTargetDate(LocalDate targetDate, Long version) {
        return new UpdateGoalRequest("Race", null, null, null, null, null,
                null, null, targetDate, null, null, null, version);
    }

    // ---------- create ----------

    @Test
    void create_setsOwnerFromArgument_andCopiesFields() {
        when(goalRepo.save(any(Goal.class))).thenAnswer(returnsFirstArg());
        var request = new CreateGoalRequest("Run", "Morning", GoalType.TARGET, TrackingType.COUNT,
                GoalPeriod.WEEKLY, 3, new BigDecimal("5.0"), "km",
                LocalTime.of(7, 0), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                7L, 8L, "Feel stronger", "run", "#FF5722");

        Goal result = goalService.create(USER_ID, request);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getTitle()).isEqualTo("Run");
        assertThat(result.getDescription()).isEqualTo("Morning");
        assertThat(result.getGoalType()).isEqualTo(GoalType.TARGET);
        assertThat(result.getTrackingType()).isEqualTo(TrackingType.COUNT);
        assertThat(result.getPeriod()).isEqualTo(GoalPeriod.WEEKLY);
        assertThat(result.getTimesPerPeriod()).isEqualTo(3);
        assertThat(result.getAmountPerSession()).isEqualTo(new BigDecimal("5.0"));
        assertThat(result.getUnit()).isEqualTo("km");
        assertThat(result.getTargetTime()).isEqualTo(LocalTime.of(7, 0));
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.getTargetDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(result.getCategoryId()).isEqualTo(7L);
        assertThat(result.getTemplateId()).isEqualTo(8L);
        assertThat(result.getMotivation()).isEqualTo("Feel stronger");
        assertThat(result.getIconKey()).isEqualTo("run");
        assertThat(result.getColor()).isEqualTo("#FF5722");
        assertThat(result.getStatus()).isEqualTo(GoalStatus.ACTIVE);
    }

    @Test
    void create_withoutTypes_defaultsToHabitAndCount() {
        when(goalRepo.save(any(Goal.class))).thenAnswer(returnsFirstArg());
        var request = createRequest(null, GoalPeriod.DAILY, 1, null, null, null, null, null);

        Goal result = goalService.create(USER_ID, request);

        assertThat(result.getGoalType()).isEqualTo(GoalType.HABIT);
        assertThat(result.getTrackingType()).isEqualTo(TrackingType.COUNT);
    }

    @Test
    void create_amountWithoutPeriod_defaultsToDailyOnce() {
        when(goalRepo.save(any(Goal.class))).thenAnswer(returnsFirstArg());
        var request = createRequest(TrackingType.AMOUNT, null, null, new BigDecimal("3"), "L",
                null, null, null);

        Goal result = goalService.create(USER_ID, request);

        assertThat(result.getPeriod()).isEqualTo(GoalPeriod.DAILY);
        assertThat(result.getTimesPerPeriod()).isEqualTo(1);
    }

    @Test
    void create_checkGoalWithoutDetails_defaultsToDailyOnce() {
        when(goalRepo.save(any(Goal.class))).thenAnswer(returnsFirstArg());
        var request = createRequest(TrackingType.CHECK, null, null, null, null, null, null, null);

        Goal result = goalService.create(USER_ID, request);

        assertThat(result.getPeriod()).isEqualTo(GoalPeriod.DAILY);
        assertThat(result.getTimesPerPeriod()).isEqualTo(1);
    }

    @Test
    void create_timeTargetWithTime_isAccepted() {
        when(goalRepo.save(any(Goal.class))).thenAnswer(returnsFirstArg());
        var request = createRequest(TrackingType.TIME_TARGET, null, null, null, null,
                LocalTime.of(23, 0), null, null);

        Goal result = goalService.create(USER_ID, request);

        assertThat(result.getTargetTime()).isEqualTo(LocalTime.of(23, 0));
        assertThat(result.getPeriod()).isEqualTo(GoalPeriod.DAILY);
    }

    @Test
    void create_eventWithFutureDate_keepsPeriodNull() {
        when(goalRepo.save(any(Goal.class))).thenAnswer(returnsFirstArg());
        var request = createRequest(TrackingType.EVENT, null, null, null, null, null, null,
                LocalDate.now().plusDays(30));

        Goal result = goalService.create(USER_ID, request);

        assertThat(result.getPeriod()).isNull();
        assertThat(result.getTimesPerPeriod()).isEqualTo(1);
    }

    @Test
    void create_countWithoutPeriod_throwsBadRequest() {
        var request = createRequest(TrackingType.COUNT, null, 3, null, null, null, null, null);

        assertThatThrownBy(() -> goalService.create(USER_ID, request))
                .isInstanceOfSatisfying(InvalidGoalException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(goalRepo, never()).save(any());
    }

    @Test
    void create_countWithoutTimes_throwsBadRequest() {
        var request = createRequest(TrackingType.COUNT, GoalPeriod.WEEKLY, null, null, null, null, null, null);

        assertThatThrownBy(() -> goalService.create(USER_ID, request))
                .isInstanceOf(InvalidGoalException.class);
        verify(goalRepo, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = TrackingType.class, names = {"AMOUNT", "DURATION", "LIMIT"})
    void create_amountBasedWithoutAmount_throwsBadRequest(TrackingType type) {
        var request = createRequest(type, null, null, null, "L", null, null, null);

        assertThatThrownBy(() -> goalService.create(USER_ID, request))
                .isInstanceOf(InvalidGoalException.class);
        verify(goalRepo, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = TrackingType.class, names = {"AMOUNT", "DURATION", "LIMIT"})
    void create_amountBasedWithoutUnit_throwsBadRequest(TrackingType type) {
        var request = createRequest(type, null, null, new BigDecimal("2"), null, null, null, null);

        assertThatThrownBy(() -> goalService.create(USER_ID, request))
                .isInstanceOf(InvalidGoalException.class);
        verify(goalRepo, never()).save(any());
    }

    @Test
    void create_timeTargetWithoutTime_throwsBadRequest() {
        var request = createRequest(TrackingType.TIME_TARGET, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> goalService.create(USER_ID, request))
                .isInstanceOf(InvalidGoalException.class);
        verify(goalRepo, never()).save(any());
    }

    @Test
    void create_eventWithoutDate_throwsBadRequest() {
        var request = createRequest(TrackingType.EVENT, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> goalService.create(USER_ID, request))
                .isInstanceOf(InvalidGoalException.class);
        verify(goalRepo, never()).save(any());
    }

    @Test
    void create_eventWithPastDate_throwsBadRequest() {
        var request = createRequest(TrackingType.EVENT, null, null, null, null, null, null,
                LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> goalService.create(USER_ID, request))
                .isInstanceOf(InvalidGoalException.class);
        verify(goalRepo, never()).save(any());
    }

    @Test
    void create_targetDateBeforeStartDate_throwsBadRequest() {
        var request = createRequest(TrackingType.EVENT, null, null, null, null, null,
                LocalDate.now().plusDays(20), LocalDate.now().plusDays(10));

        assertThatThrownBy(() -> goalService.create(USER_ID, request))
                .isInstanceOf(InvalidGoalException.class);
        verify(goalRepo, never()).save(any());
    }

    // ---------- read ----------

    @Test
    void findAll_withoutStatus_usesUnfilteredQuery() {
        List<Goal> goals = List.of(existingGoal());
        when(goalRepo.findAllByUserIdOrderByCreatedAtDesc(USER_ID)).thenReturn(goals);

        assertThat(goalService.findAll(USER_ID, null)).isEqualTo(goals);
        verify(goalRepo, never()).findAllByUserIdAndStatusOrderByCreatedAtDesc(any(), any());
    }

    @Test
    void findAll_withStatus_usesFilteredQuery() {
        List<Goal> goals = List.of(existingGoal());
        when(goalRepo.findAllByUserIdAndStatusOrderByCreatedAtDesc(USER_ID, GoalStatus.ARCHIVED))
                .thenReturn(goals);

        assertThat(goalService.findAll(USER_ID, GoalStatus.ARCHIVED)).isEqualTo(goals);
        verify(goalRepo, never()).findAllByUserIdOrderByCreatedAtDesc(any());
    }

    @Test
    void findById_missingOrNotOwned_throwsNotFound() {
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.findById(USER_ID, GOAL_ID))
                .isInstanceOf(GoalNotFoundException.class)
                .hasMessage("Goal with id 5 not found")
                .isInstanceOfSatisfying(GoalNotFoundException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void findById_ownGoal_returnsIt() {
        Goal goal = existingGoal();
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));

        assertThat(goalService.findById(USER_ID, GOAL_ID)).isSameAs(goal);
    }

    // ---------- update ----------

    @Test
    void update_replacesAllEditableFields() {
        Goal goal = existingGoal();
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        when(goalRepo.saveAndFlush(goal)).thenReturn(goal);
        var request = updateRequest("Run more", null, GoalPeriod.DAILY, 1, null, null);

        Goal result = goalService.update(USER_ID, GOAL_ID, request);

        assertThat(result.getTitle()).isEqualTo("Run more");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getPeriod()).isEqualTo(GoalPeriod.DAILY);
        assertThat(result.getTimesPerPeriod()).isEqualTo(1);
        assertThat(result.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void update_keepsTrackingTypeAndGoalType() {
        Goal goal = existingGoal();
        goal.setTrackingType(TrackingType.AMOUNT);
        goal.setGoalType(GoalType.TARGET);
        goal.setCategoryId(7L);
        goal.setTemplateId(8L);
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        when(goalRepo.saveAndFlush(goal)).thenReturn(goal);
        var request = updateRequest("Drink water", null, null, null, new BigDecimal("2.5"), "L");

        Goal result = goalService.update(USER_ID, GOAL_ID, request);

        assertThat(result.getTrackingType()).isEqualTo(TrackingType.AMOUNT);
        assertThat(result.getGoalType()).isEqualTo(GoalType.TARGET);
        assertThat(result.getCategoryId()).isEqualTo(7L);
        assertThat(result.getTemplateId()).isEqualTo(8L);
    }

    @Test
    void update_amountGoalWithoutAmount_throwsBadRequest() {
        Goal goal = existingGoal();
        goal.setTrackingType(TrackingType.AMOUNT);
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        var request = updateRequest("Drink water", null, null, null, null, null);

        assertThatThrownBy(() -> goalService.update(USER_ID, GOAL_ID, request))
                .isInstanceOf(InvalidGoalException.class);
        verify(goalRepo, never()).saveAndFlush(any());
    }

    @Test
    void update_matchingVersion_isAccepted() {
        Goal goal = existingGoal();
        goal.setVersion(2L);
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        when(goalRepo.saveAndFlush(goal)).thenReturn(goal);
        var request = new UpdateGoalRequest("Run", null, GoalPeriod.WEEKLY, 3, null, null,
                null, null, null, null, null, null, 2L);

        assertThat(goalService.update(USER_ID, GOAL_ID, request)).isSameAs(goal);
    }

    @Test
    void update_staleVersion_throwsOptimisticLockFailure() {
        Goal goal = existingGoal();
        goal.setVersion(2L);
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        var request = new UpdateGoalRequest("Run", null, GoalPeriod.WEEKLY, 3, null, null,
                null, null, null, null, null, null, 1L);

        assertThatThrownBy(() -> goalService.update(USER_ID, GOAL_ID, request))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        verify(goalRepo, never()).saveAndFlush(any());
    }

    @Test
    void update_eventWithUnchangedPastDate_isAllowed() {
        LocalDate pastDate = LocalDate.now().minusDays(5);
        Goal goal = existingGoal();
        goal.setTrackingType(TrackingType.EVENT);
        goal.setTargetDate(pastDate);
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        when(goalRepo.saveAndFlush(goal)).thenReturn(goal);

        Goal result = goalService.update(USER_ID, GOAL_ID, updateRequestWithTargetDate(pastDate, null));

        assertThat(result.getTargetDate()).isEqualTo(pastDate);
    }

    @Test
    void update_eventWithNewPastDate_throwsBadRequest() {
        Goal goal = existingGoal();
        goal.setTrackingType(TrackingType.EVENT);
        goal.setTargetDate(LocalDate.now().plusDays(10));
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));

        assertThatThrownBy(() -> goalService.update(USER_ID, GOAL_ID,
                updateRequestWithTargetDate(LocalDate.now().minusDays(1), null)))
                .isInstanceOf(InvalidGoalException.class);
        verify(goalRepo, never()).saveAndFlush(any());
    }

    @Test
    void update_archivedGoal_throwsAndDoesNotSave() {
        Goal goal = existingGoal();
        goal.setStatus(GoalStatus.ARCHIVED);
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        var request = updateRequest("Run more", null, GoalPeriod.DAILY, 1, null, null);

        assertThatThrownBy(() -> goalService.update(USER_ID, GOAL_ID, request))
                .isInstanceOf(GoalArchivedException.class);

        verify(goalRepo, never()).saveAndFlush(any());
        assertThat(goal.getTitle()).isEqualTo("Run");
    }

    // ---------- archive / unarchive / delete ----------

    @Test
    void archive_setsStatusAndTimestamp() {
        Goal goal = existingGoal();
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        when(goalRepo.saveAndFlush(goal)).thenReturn(goal);

        Goal result = goalService.archive(USER_ID, GOAL_ID);

        assertThat(result.getStatus()).isEqualTo(GoalStatus.ARCHIVED);
        assertThat(result.getArchivedAt()).isNotNull();
    }

    @Test
    void archive_alreadyArchived_keepsOriginalTimestamp() {
        Goal goal = existingGoal();
        Instant archivedAt = Instant.parse("2026-01-01T00:00:00Z");
        goal.setStatus(GoalStatus.ARCHIVED);
        goal.setArchivedAt(archivedAt);
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        when(goalRepo.saveAndFlush(goal)).thenReturn(goal);

        assertThat(goalService.archive(USER_ID, GOAL_ID).getArchivedAt()).isEqualTo(archivedAt);
    }

    @Test
    void unarchive_archivedGoal_setsActiveAndClearsTimestamp() {
        Goal goal = existingGoal();
        goal.setStatus(GoalStatus.ARCHIVED);
        goal.setArchivedAt(Instant.parse("2026-01-01T00:00:00Z"));
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        when(goalRepo.saveAndFlush(goal)).thenReturn(goal);

        Goal result = goalService.unarchive(USER_ID, GOAL_ID);

        assertThat(result.getStatus()).isEqualTo(GoalStatus.ACTIVE);
        assertThat(result.getArchivedAt()).isNull();
    }

    @Test
    void unarchive_activeGoal_changesNothing() {
        Goal goal = existingGoal();
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        when(goalRepo.saveAndFlush(goal)).thenReturn(goal);

        Goal result = goalService.unarchive(USER_ID, GOAL_ID);

        assertThat(result.getStatus()).isEqualTo(GoalStatus.ACTIVE);
        assertThat(result.getArchivedAt()).isNull();
    }

    @Test
    void unarchive_notOwned_throwsNotFound() {
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.unarchive(USER_ID, GOAL_ID))
                .isInstanceOf(GoalNotFoundException.class);
    }

    @Test
    void delete_archivedOwnedGoal_isDeleted() {
        Goal goal = existingGoal();
        goal.setStatus(GoalStatus.ARCHIVED);
        goal.setArchivedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));

        goalService.delete(USER_ID, GOAL_ID);

        verify(goalRepo).delete(goal);
    }

    @Test
    void delete_notOwned_throwsAndDeletesNothing() {
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.delete(USER_ID, GOAL_ID))
                .isInstanceOf(GoalNotFoundException.class);
        verify(goalRepo, never()).delete(any(Goal.class));
    }

    @Test
    void delete_activeGoal_throwsAndDoesNotDelete() {
        Goal goal = existingGoal();
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));

        assertThatThrownBy(() -> goalService.delete(USER_ID, GOAL_ID))
                .isInstanceOf(GoalNotArchivedException.class);

        verify(goalRepo, never()).delete(any());
    }
}