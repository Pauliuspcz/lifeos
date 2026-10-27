package no.kristiania.goalservice.integration.setup;

import com.jayway.jsonpath.JsonPath;
import org.springframework.test.web.servlet.ResultActions;

import static no.kristiania.goalservice.integration.setup.GoalPayload.valid;
import static no.kristiania.goalservice.integration.setup.JwtTestSupport.bearer;
import static no.kristiania.goalservice.integration.setup.JwtTestSupport.tokenFor;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractGoalApiTest extends AbstractIntegrationTest {

    protected static final String VALID_GOAL = """
            {"title":"Run","description":"Morning runs","period":"WEEKLY",
             "timesPerPeriod":3,"amountPerSession":5.0,"unit":"km"}""";

    protected static String authHeader(long userId) { return bearer(tokenFor(userId)); }

    protected static String bodyOf(ResultActions result) throws Exception {
        return result.andReturn().getResponse().getContentAsString();
    }

    protected ResultActions createGoal(long userId, GoalPayload payload) throws Exception {
        return createGoal(userId, payload.json());
    }

    protected ResultActions createGoal(long userId, String body) throws Exception {
        return mockMvc.perform(post("/api/goal")
                .header(AUTHORIZATION, authHeader(userId))
                .contentType(APPLICATION_JSON).content(body));
    }

    protected long createGoalId(long userId) throws Exception { return createGoalId(userId, valid()); }

    protected long createGoalId(long userId, GoalPayload payload) throws Exception {
        String response = bodyOf(createGoal(userId, payload).andExpect(status().isCreated()));
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    protected ResultActions getGoal(long userId, long id) throws Exception {
        return mockMvc.perform(get("/api/goal/{id}", id).header(AUTHORIZATION, authHeader(userId)));
    }

    protected ResultActions listGoals(long userId) throws Exception {
        return mockMvc.perform(get("/api/goal").header(AUTHORIZATION, authHeader(userId)));
    }

    protected ResultActions listGoals(long userId, String status) throws Exception {
        return mockMvc.perform(get("/api/goal").param("status", status)
                .header(AUTHORIZATION, authHeader(userId)));
    }

    protected ResultActions updateGoal(long userId, long id, GoalPayload payload) throws Exception {
        return mockMvc.perform(put("/api/goal/{id}", id)
                .header(AUTHORIZATION, authHeader(userId))
                .contentType(APPLICATION_JSON).content(payload.json()));
    }

    protected ResultActions archiveGoal(long userId, long id) throws Exception {
        return mockMvc.perform(patch("/api/goal/{id}/archive", id).header(AUTHORIZATION, authHeader(userId)));
    }

    protected ResultActions unarchiveGoal(long userId, long id) throws Exception {
        return mockMvc.perform(patch("/api/goal/{id}/unarchive", id).header(AUTHORIZATION, authHeader(userId)));
    }

    protected ResultActions deleteGoal(long userId, long id) throws Exception {
        return mockMvc.perform(delete("/api/goal/{id}", id).header(AUTHORIZATION, authHeader(userId)));
    }
}