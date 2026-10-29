package no.kristiania.goalservice.integration.goal;

import no.kristiania.goalservice.integration.setup.AbstractGoalApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.jdbc.JdbcTestUtils;

import static no.kristiania.goalservice.integration.setup.GoalPayload.valid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GoalDeleteTest extends AbstractGoalApiTest {

    private static final long USER = 1L;

    @Test
    void delete_archivedGoal_returns204_andRemovesTheRow() throws Exception {
        long id = createGoalId(USER);
        archiveGoal(USER, id).andExpect(status().isOk());

        deleteGoal(USER, id).andExpect(status().isNoContent());

        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "goals")).isZero();
        getGoal(USER, id).andExpect(status().isNotFound());
    }

    @Test
    void delete_activeGoal_returns409_andKeepsTheGoal() throws Exception {
        long id = createGoalId(USER);

        deleteGoal(USER, id)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Goal must be archived before it can be deleted"));

        String status = jdbcTemplate.queryForObject(
                "select status from goals where id = ?", String.class, id);
        assertThat(status).isEqualTo("ACTIVE");
    }

    @Test
    void delete_unknownId_returns404() throws Exception {
        deleteGoal(USER, 999_999L)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal with id 999999 not found"));
    }

    @Test
    void delete_sameGoalTwice_secondCallReturns404() throws Exception {
        long id = createGoalId(USER);
        archiveGoal(USER, id).andExpect(status().isOk());
        deleteGoal(USER, id).andExpect(status().isNoContent());

        deleteGoal(USER, id).andExpect(status().isNotFound());
    }

    @Test
    void delete_leavesOtherGoalsUntouched() throws Exception {
        long doomed = createGoalId(USER, valid().with("title", "Doomed"));
        long survivor = createGoalId(USER, valid().with("title", "Survivor"));
        archiveGoal(USER, doomed).andExpect(status().isOk());

        deleteGoal(USER, doomed).andExpect(status().isNoContent());

        getGoal(USER, survivor)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Survivor"));
    }
}
