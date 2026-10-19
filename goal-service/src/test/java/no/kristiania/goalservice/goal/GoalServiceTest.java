package no.kristiania.goalservice.goal;

import no.kristiania.goalservice.goal.dto.CreateGoalRequest;
import no.kristiania.goalservice.goal.dto.UpdateGoalRequest;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import no.kristiania.goalservice.goal.enums.GoalStatus;
import no.kristiania.goalservice.goal.exceptions.GoalNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
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
    private GoalRepo goalRepo;

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

    @Test
    void create_setsOwnerFromArgument_andCopiesFields() {
        when(goalRepo.save(any(Goal.class))).thenAnswer(returnsFirstArg());
        var request = new CreateGoalRequest("Run", "Morning", GoalPeriod.WEEKLY, 3, new BigDecimal("5.0"), "km");

        Goal result = goalService.create(USER_ID, request);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getTitle()).isEqualTo("Run");
        assertThat(result.getDescription()).isEqualTo("Morning");
        assertThat(result.getPeriod()).isEqualTo(GoalPeriod.WEEKLY);
        assertThat(result.getTimesPerPeriod()).isEqualTo(3);
        assertThat(result.getAmountPerSession()).isEqualTo(new BigDecimal("5.0"));
        assertThat(result.getUnit()).isEqualTo("km");
        assertThat(result.getStatus()).isEqualTo(GoalStatus.ACTIVE);
    }

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
    void update_replacesAllEditableFields() {
        Goal goal = existingGoal();
        when(goalRepo.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        when(goalRepo.saveAndFlush(goal)).thenReturn(goal);
        var request = new UpdateGoalRequest("Run more", null, GoalPeriod.DAILY, 1, null, null);

        Goal result = goalService.update(USER_ID, GOAL_ID, request);

        assertThat(result.getTitle()).isEqualTo("Run more");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getPeriod()).isEqualTo(GoalPeriod.DAILY);
        assertThat(result.getTimesPerPeriod()).isEqualTo(1);
        assertThat(result.getUserId()).isEqualTo(USER_ID);
    }

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
    void delete_ownedGoal_isDeleted() {
        Goal goal = existingGoal();
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
}
