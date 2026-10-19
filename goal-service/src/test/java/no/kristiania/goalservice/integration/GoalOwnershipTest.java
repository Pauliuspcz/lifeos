package no.kristiania.goalservice.integration;

import org.junit.jupiter.api.Test;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GoalOwnershipTest extends AbstractGoalApiTest {

    private static final long ALICE = 1L;
    private static final long BOB = 2L;

    @Test
    void user_cannotReadAnotherUsersGoal() throws Exception {
        long id = createGoalId(ALICE);
        getGoal(BOB, id).andExpect(status().isNotFound());
    }

    @Test
    void user_cannotUpdateAnotherUsersGoal() throws Exception {
        long id = createGoalId(ALICE);

        mockMvc.perform(put("/goals/{id}", id)
                        .header(AUTHORIZATION, authHeader(BOB))
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Hacked\",\"period\":\"DAILY\",\"timesPerPeriod\":1}"))
                .andExpect(status().isNotFound());

        getGoal(ALICE, id).andExpect(jsonPath("$.title").value("Run"));
    }

    @Test
    void user_cannotArchiveAnotherUsersGoal() throws Exception {
        long id = createGoalId(ALICE);

        mockMvc.perform(patch("/goals/{id}/archive", id).header(AUTHORIZATION, authHeader(BOB)))
                .andExpect(status().isNotFound());

        getGoal(ALICE, id).andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void user_cannotDeleteAnotherUsersGoal() throws Exception {
        long id = createGoalId(ALICE);

        mockMvc.perform(delete("/goals/{id}", id).header(AUTHORIZATION, authHeader(BOB)))
                .andExpect(status().isNotFound());

        getGoal(ALICE, id).andExpect(status().isOk());
    }

    @Test
    void list_containsOnlyOwnGoals() throws Exception {
        createGoalId(ALICE);
        createGoalId(ALICE);
        createGoalId(BOB);

        mockMvc.perform(get("/goals").header(AUTHORIZATION, authHeader(ALICE)))
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/goals").header(AUTHORIZATION, authHeader(BOB)))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void create_ignoresUserIdInBody_andUsesTokenSubject() throws Exception {
        String response = createGoal(ALICE, """
                {"userId":2,"title":"Run","period":"WEEKLY","timesPerPeriod":3}""")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) com.jayway.jsonpath.JsonPath.read(response, "$.id")).longValue();

        getGoal(ALICE, id).andExpect(status().isOk());
        getGoal(BOB, id).andExpect(status().isNotFound());
    }
}
