package no.kristiania.goalservice.integration;

import com.jayway.jsonpath.JsonPath;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

abstract class AbstractGoalApiTest extends AbstractIntegrationTest {

    protected static final String VALID_GOAL = """
            {"title":"Run","description":"Morning runs","period":"WEEKLY",
             "timesPerPeriod":3,"amountPerSession":5.0,"unit":"km"}""";

    protected static String authHeader(long userId) {
        return bearer(tokenFor(userId));
    }

    protected ResultActions createGoal(long userId, String body) throws Exception {
        return mockMvc.perform(post("/api/goal")
                .header(AUTHORIZATION, authHeader(userId))
                .contentType(APPLICATION_JSON)
                .content(body));
    }

    protected long createGoalId(long userId) throws Exception {
        String response = createGoal(userId, VALID_GOAL).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    protected ResultActions getGoal(long userId, long goalId) throws Exception {
        return mockMvc.perform(get("/api/goal/{id}", goalId).header(AUTHORIZATION, authHeader(userId)));
    }
}