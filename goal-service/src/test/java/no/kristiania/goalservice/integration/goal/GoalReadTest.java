package no.kristiania.goalservice.integration.goal;

import no.kristiania.goalservice.integration.setup.AbstractGoalApiTest;
import org.junit.jupiter.api.Test;

import static no.kristiania.goalservice.integration.setup.GoalPayload.valid;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.nullValue;

class GoalReadTest extends AbstractGoalApiTest {

    private static final long USER = 1L;

    @Test
    void get_returnsTheGoal() throws Exception {
        long id = createGoalId(USER, valid().with("title", "Swim").with("period", "MONTHLY"));

        getGoal(USER, id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Swim"))
                .andExpect(jsonPath("$.period").value("MONTHLY"));
    }

    @Test
    void get_unknownId_returns404() throws Exception {
        getGoal(USER, 999_999L)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal with id 999999 not found"));
    }

    @Test
    void list_isEmpty_forUserWithoutGoals() throws Exception {
        listGoals(USER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void list_returnsNewestFirst() throws Exception {
        createGoalId(USER, valid().with("title", "First"));
        createGoalId(USER, valid().with("title", "Second"));
        createGoalId(USER, valid().with("title", "Third"));

        listGoals(USER)
                .andExpect(jsonPath("$[0].title").value("Third"))
                .andExpect(jsonPath("$[1].title").value("Second"))
                .andExpect(jsonPath("$[2].title").value("First"));
    }

    @Test
    void list_filtersOnStatus() throws Exception {
        long archivedId = createGoalId(USER, valid().with("title", "Old"));
        createGoalId(USER, valid().with("title", "Current"));
        archiveGoal(USER, archivedId).andExpect(status().isOk());

        listGoals(USER).andExpect(jsonPath("$.length()").value(2));
        listGoals(USER, "ACTIVE")
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Current"));
        listGoals(USER, "ARCHIVED")
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(archivedId));
    }

    @Test
    void get_archivedGoal_canStillBeRead() throws Exception {
        long id = createGoalId(USER);
        archiveGoal(USER, id).andExpect(status().isOk());

        getGoal(USER, id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"))
                .andExpect(jsonPath("$.archivedAt").isNotEmpty());
    }

    @Test
    void templateGoal_returnsTemplateAndDirectionCode() throws Exception {
        long templateId = jdbcTemplate.queryForObject(
                "select id from goal_templates where code = 'WALK_10K_STEPS'", Long.class);
        long categoryId = jdbcTemplate.queryForObject(
                "select category_id from goal_templates where code = 'WALK_10K_STEPS'", Long.class);
        long id = createGoalId(USER, valid().with("templateId", templateId).with("categoryId", categoryId));

        getGoal(USER, id)
                .andExpect(jsonPath("$.templateCode").value("WALK_10K_STEPS"))
                .andExpect(jsonPath("$.directionCode").value("MOVE_MORE"));
        listGoals(USER)
                .andExpect(jsonPath("$[0].templateCode").value("WALK_10K_STEPS"))
                .andExpect(jsonPath("$[0].directionCode").value("MOVE_MORE"));
    }

    @Test
    void customGoal_hasNoTemplateCodes() throws Exception {
        long id = createGoalId(USER);

        getGoal(USER, id)
                .andExpect(jsonPath("$.templateCode").value(nullValue()))
                .andExpect(jsonPath("$.directionCode").value(nullValue()));
    }
}
