package no.kristiania.progressservice.integration.setup;

import no.kristiania.progressservice.client.GoalSummary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.ZoneId;
import com.jayway.jsonpath.JsonPath;
import static no.kristiania.progressservice.integration.setup.JwtTestSupport.bearer;
import static no.kristiania.progressservice.integration.setup.JwtTestSupport.tokenFor;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public abstract class AbstractProgressApiTest extends AbstractIntegrationTest {

    protected static final LocalDate TODAY = LocalDate.now(ZoneId.of("Europe/Oslo"));

    protected ResultActions postProgress(long userId, long goalId, LocalDate date) throws Exception {
        return mockMvc.perform(post("/api/progress")
                .header(AUTHORIZATION, bearer(tokenFor(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"goalId": %d, "date": "%s"}""".formatted(goalId, date)));
    }

    protected void goalHasStatus(long goalId, String status) {
        when(goalClient.getGoal(goalId)).thenReturn(new GoalSummary(
                goalId, status, "CHECK", 1, "DAILY", null, null, TODAY, null));
    }
    protected long createProgress(long userId, long goalId) throws Exception {
        goalHasStatus(goalId, "ACTIVE");
        String body = postProgress(userId, goalId, TODAY)
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    protected ResultActions getProgress(long userId) throws Exception {
        return mockMvc.perform(get("/api/progress")
                .header(AUTHORIZATION, bearer(tokenFor(userId))));
    }

    protected ResultActions getWindow(long userId) throws Exception {
        return mockMvc.perform(get("/api/progress/window")
                .header(AUTHORIZATION, bearer(tokenFor(userId))));
    }

    protected ResultActions deleteProgress(long userId, long progressId) throws Exception {
        return mockMvc.perform(delete("/api/progress/{id}", progressId)
                .header(AUTHORIZATION, bearer(tokenFor(userId))));
    }
}
