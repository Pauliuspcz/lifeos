package no.kristiania.goalservice.integration.ownership;

import no.kristiania.goalservice.integration.setup.AbstractGoalApiTest;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static no.kristiania.goalservice.integration.setup.GoalPayload.valid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GoalOwnershipTest extends AbstractGoalApiTest {

    private static final long OWNER = 1L;
    private static final long OTHER = 2L;

    @FunctionalInterface
    interface Call {
        ResultActions run(long userId, long goalId) throws Exception;
    }

    Stream<Arguments> operationsOnSingleGoal() {
        return Stream.of(
                Arguments.of(Named.of("GET", (Call) this::getGoal)),
                Arguments.of(Named.of("PUT", (Call) (user, id) -> updateGoal(user, id, valid().with("title", "Hijacked")))),
                Arguments.of(Named.of("PATCH archive", (Call) this::archiveGoal)),
                Arguments.of(Named.of("PATCH unarchive", (Call) this::unarchiveGoal)),
                Arguments.of(Named.of("DELETE", (Call) this::deleteGoal)));
    }

    // ---------- L9.1 ----------

    @ParameterizedTest(name = "{0} on another user's goal returns 404")
    @MethodSource("operationsOnSingleGoal")
    void operation_onOtherUsersGoal_returns404(Call operation) throws Exception {
        // Given a goal owned by OWNER
        long id = createGoalId(OWNER);

        // When another user tries the operation
        ResultActions result = operation.run(OTHER, id);

        // Then it looks like the goal does not exist
        result.andExpect(status().isNotFound());
    }

    @ParameterizedTest(name = "{0} by another user leaves the goal unchanged")
    @MethodSource("operationsOnSingleGoal")
    void operation_byOtherUser_leavesGoalUnchanged(Call operation) throws Exception {
        // Given a goal owned by OWNER, and a snapshot of it
        long id = createGoalId(OWNER);
        String before = bodyOf(getGoal(OWNER, id));

        // When another user tries the operation
        operation.run(OTHER, id);

        // Then the owner still sees exactly the same goal
        assertThat(bodyOf(getGoal(OWNER, id))).isEqualTo(before);
    }

    @Test
    void list_returnsOnlyOwnGoals() throws Exception {
        // Given two goals for OWNER and one for OTHER
        createGoalId(OWNER, valid().with("title", "owner-1"));
        createGoalId(OWNER, valid().with("title", "owner-2"));
        createGoalId(OTHER, valid().with("title", "other-1"));

        // When OWNER lists goals
        ResultActions result = listGoals(OWNER);

        // Then only OWNER's two goals are returned
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(org.hamcrest.Matchers.containsInAnyOrder("owner-1", "owner-2")));
    }
}
