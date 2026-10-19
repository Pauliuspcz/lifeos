package no.kristiania.goalservice.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.jdbc.JdbcTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GoalApiTest extends AbstractGoalApiTest {

    private static final long USER = 1L;

    @Test
    void create_returns201_withLocationAndDefaults() throws Exception {
        createGoal(USER, VALID_GOAL)
                .andExpect(status().isCreated())
                .andExpect(header().exists(LOCATION))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Run"))
                .andExpect(jsonPath("$.period").value("WEEKLY"))
                .andExpect(jsonPath("$.timesPerPeriod").value(3))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void create_withInvalidBody_returns400_andStoresNothing() throws Exception {
        createGoal(USER, """
                {"title":" ","period":null,"timesPerPeriod":0}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.period").exists())
                .andExpect(jsonPath("$.fieldErrors.timesPerPeriod").exists());

        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "goals")).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{not json",
            "",
            "{\"title\":\"Run\",\"period\":\"HOURLY\",\"timesPerPeriod\":1}"
    })
    void create_withUnreadableBody_returns400(String body) throws Exception {
        createGoal(USER, body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));
    }

    @Test
    void list_returnsOwnGoals_andFiltersOnStatus() throws Exception {
        long archivedId = createGoalId(USER);
        createGoalId(USER);
        mockMvc.perform(patch("/goals/{id}/archive", archivedId)
                .header(AUTHORIZATION, authHeader(USER))).andExpect(status().isOk());

        mockMvc.perform(get("/goals").header(AUTHORIZATION, authHeader(USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/goals").param("status", "ACTIVE")
                        .header(AUTHORIZATION, authHeader(USER)))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        mockMvc.perform(get("/goals").param("status", "ARCHIVED")
                        .header(AUTHORIZATION, authHeader(USER)))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(archivedId));
    }

    @Test
    void list_withInvalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/goals").param("status", "NOPE")
                        .header(AUTHORIZATION, authHeader(USER)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'status'"));
    }

    @Test
    void get_returnsGoal() throws Exception {
        long id = createGoalId(USER);
        getGoal(USER, id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Run"));
    }

    @Test
    void get_unknownId_returns404() throws Exception {
        getGoal(USER, 999_999L)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal with id 999999 not found"));
    }

    @Test
    void update_replacesFields_andBumpsVersion() throws Exception {
        long id = createGoalId(USER);

        mockMvc.perform(put("/goals/{id}", id)
                        .header(AUTHORIZATION, authHeader(USER))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Run more","description":null,"period":"DAILY",
                                 "timesPerPeriod":1,"amountPerSession":10,"unit":"km"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Run more"))
                .andExpect(jsonPath("$.period").value("DAILY"))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void update_withInvalidBody_returns400() throws Exception {
        long id = createGoalId(USER);

        mockMvc.perform(put("/goals/{id}", id)
                        .header(AUTHORIZATION, authHeader(USER))
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"\",\"period\":\"DAILY\",\"timesPerPeriod\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    void archive_setsStatusAndTimestamp_andIsIdempotent() throws Exception {
        long id = createGoalId(USER);

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(patch("/goals/{id}/archive", id).header(AUTHORIZATION, authHeader(USER)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ARCHIVED"))
                    .andExpect(jsonPath("$.archivedAt").isNotEmpty());
        }
    }

    @Test
    void delete_returns204_thenGoalIsGone() throws Exception {
        long id = createGoalId(USER);

        mockMvc.perform(delete("/goals/{id}", id).header(AUTHORIZATION, authHeader(USER)))
                .andExpect(status().isNoContent());
        getGoal(USER, id).andExpect(status().isNotFound());
    }
}