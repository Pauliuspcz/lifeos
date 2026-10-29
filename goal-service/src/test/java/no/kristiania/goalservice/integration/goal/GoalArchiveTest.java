package no.kristiania.goalservice.integration.goal;

import com.jayway.jsonpath.JsonPath;
import no.kristiania.goalservice.integration.setup.AbstractGoalApiTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GoalArchiveTest extends AbstractGoalApiTest {

    private static final long USER = 1L;

    @Test
    void archive_setsStatusAndTimestamp() throws Exception {
        long id = createGoalId(USER);

        archiveGoal(USER, id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"))
                .andExpect(jsonPath("$.archivedAt").isNotEmpty())
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void archive_calledTwice_keepsOriginalTimestampAndVersion() throws Exception {
        long id = createGoalId(USER);
        String first = bodyOf(archiveGoal(USER, id).andExpect(status().isOk()));

        String second = bodyOf(archiveGoal(USER, id).andExpect(status().isOk()));

        assertThat((String) JsonPath.read(second, "$.archivedAt"))
                .isEqualTo(JsonPath.read(first, "$.archivedAt"));
        assertThat((Integer) JsonPath.read(second, "$.version"))
                .isEqualTo(JsonPath.read(first, "$.version"));
    }

    @Test
    void archive_unknownId_returns404() throws Exception {
        archiveGoal(USER, 999_999L)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal with id 999999 not found"));
    }

    @Test
    void archivedGoal_appearsInArchivedFilter_notInActive() throws Exception {
        long id = createGoalId(USER);
        archiveGoal(USER, id).andExpect(status().isOk());

        listGoals(USER, "ARCHIVED").andExpect(jsonPath("$[0].id").value(id));
        listGoals(USER, "ACTIVE").andExpect(jsonPath("$.length()").value(0));
    }
}