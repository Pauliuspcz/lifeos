package no.kristiania.goalservice.integration.goal;

import no.kristiania.goalservice.integration.setup.AbstractGoalApiTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GoalUnarchiveTest extends AbstractGoalApiTest {

    private static final long USER = 1L;

    @Test
    void unarchive_makesGoalActiveAgain() throws Exception {
        long id = createGoalId(USER);
        archiveGoal(USER, id).andExpect(status().isOk());

        unarchiveGoal(USER, id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.archivedAt").value(nullValue()))
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void unarchive_activeGoal_changesNothing() throws Exception {
        long id = createGoalId(USER);

        unarchiveGoal(USER, id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    void unarchive_unknownId_returns404_withGoalNotFoundMessage() throws Exception {
        unarchiveGoal(USER, 999_999L)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal with id 999999 not found"));
    }

    @Test
    void unarchivedGoal_appearsInActiveFilter_notInArchived() throws Exception {
        long id = createGoalId(USER);
        archiveGoal(USER, id).andExpect(status().isOk());
        unarchiveGoal(USER, id).andExpect(status().isOk());

        listGoals(USER, "ACTIVE").andExpect(jsonPath("$[0].id").value(id));
        listGoals(USER, "ARCHIVED").andExpect(jsonPath("$.length()").value(0));
    }
}
