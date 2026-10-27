package no.kristiania.goalservice.integration.goal;

import com.jayway.jsonpath.JsonPath;
import no.kristiania.goalservice.integration.setup.AbstractGoalApiTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;

import static no.kristiania.goalservice.integration.setup.GoalPayload.valid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GoalCreateTest extends AbstractGoalApiTest {

    private static final long USER = 1L;

    @Test
    void create_returns201_withLocationPointingToTheNewGoal() throws Exception {
        MvcResult result = createGoal(USER, valid())
                .andExpect(status().isCreated())
                .andExpect(header().exists(LOCATION))
                .andReturn();

        long id = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
        String location = result.getResponse().getHeader(LOCATION);
        assertThat(location).endsWith("/api/goal/" + id);
        mockMvc.perform(get(URI.create(location)).header(AUTHORIZATION, authHeader(USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void create_setsDefaultValues() throws Exception {
        String response = bodyOf(createGoal(USER, valid())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.archivedAt").value(nullValue()))
                .andExpect(jsonPath("$.templateId").value(nullValue()))
                .andExpect(jsonPath("$.userId").doesNotExist()));

        String createdAt = JsonPath.read(response, "$.createdAt");
        String updatedAt = JsonPath.read(response, "$.updatedAt");
        assertThat(createdAt).isEqualTo(updatedAt);
    }

    @Test
    void create_usesUserIdFromToken_notFromBody() throws Exception {
        createGoal(7L, valid().with("userId", 99)).andExpect(status().isCreated());

        Long storedUserId = jdbcTemplate.queryForObject("select user_id from goals", Long.class);
        assertThat(storedUserId).isEqualTo(7L);
    }

    @Test
    void create_withoutOptionalFields_returns201_andNullsInResponse() throws Exception {
        createGoal(USER, valid().without("description").without("amountPerSession").without("unit"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value(nullValue()))
                .andExpect(jsonPath("$.amountPerSession").value(nullValue()))
                .andExpect(jsonPath("$.unit").value(nullValue()));
    }

    @Test
    void create_twoGoalsWithSameTitle_areBothAccepted_withDifferentIds() throws Exception {
        long first = createGoalId(USER, valid().with("title", "Run"));
        long second = createGoalId(USER, valid().with("title", "Run"));

        assertThat(first).isNotEqualTo(second);
        listGoals(USER).andExpect(jsonPath("$.length()").value(2));
    }

    @ParameterizedTest(name = "period {0} can be stored")
    @ValueSource(strings = {"DAILY", "WEEKLY", "MONTHLY", "YEARLY"})
    void create_acceptsEveryPeriod(String period) throws Exception {
        createGoal(USER, valid().with("period", period))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.period").value(period));
    }
}
