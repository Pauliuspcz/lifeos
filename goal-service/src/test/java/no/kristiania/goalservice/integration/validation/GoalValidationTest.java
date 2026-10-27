package no.kristiania.goalservice.integration.validation;

import com.jayway.jsonpath.JsonPath;
import no.kristiania.goalservice.integration.setup.AbstractGoalApiTest;
import no.kristiania.goalservice.integration.setup.GoalPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.ResultActions;

import static no.kristiania.goalservice.integration.setup.GoalPayload.valid;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GoalValidationTest extends AbstractGoalApiTest {

    private static final long USER = 1L;
    private static final int TITLE_MAX = 255;
    private static final int DESCRIPTION_MAX = 1000;

    @ParameterizedTest(name = "{0}: title at max length is accepted")
    @ValueSource(strings = {"POST", "PUT"})
    void title_atMaxLength_isAccepted(String method) throws Exception {
        submit(method, valid().with("title", "a".repeat(TITLE_MAX)))
                .andExpect(status().is2xxSuccessful());
    }

    @ParameterizedTest(name = "{0}: title over max length is rejected")
    @ValueSource(strings = {"POST", "PUT"})
    void title_overMaxLength_isRejected(String method) throws Exception {
        submit(method, valid().with("title", "a".repeat(TITLE_MAX + 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @ParameterizedTest(name = "{0}: blank title is rejected")
    @ValueSource(strings = {"POST", "PUT"})
    void title_blank_isRejected(String method) throws Exception {
        submit(method, valid().with("title", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @ParameterizedTest(name = "{0}: missing title is rejected")
    @ValueSource(strings = {"POST", "PUT"})
    void title_missing_isRejected(String method) throws Exception {
        submit(method, valid().without("title"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @ParameterizedTest(name = "{0}: description at max length is accepted")
    @ValueSource(strings = {"POST", "PUT"})
    void description_atMaxLength_isAccepted(String method) throws Exception {
        submit(method, valid().with("description", "a".repeat(DESCRIPTION_MAX)))
                .andExpect(status().is2xxSuccessful());
    }

    @ParameterizedTest(name = "{0}: description over max length is rejected")
    @ValueSource(strings = {"POST", "PUT"})
    void description_overMaxLength_isRejected(String method) throws Exception {
        submit(method, valid().with("description", "a".repeat(DESCRIPTION_MAX + 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.description").exists());
    }

    @ParameterizedTest(name = "{0}: null description is accepted")
    @ValueSource(strings = {"POST", "PUT"})
    void description_null_isAccepted(String method) throws Exception {
        submit(method, valid().with("description", null))
                .andExpect(status().is2xxSuccessful());
    }

    @ParameterizedTest(name = "{0}: missing description is accepted")
    @ValueSource(strings = {"POST", "PUT"})
    void description_missing_isAccepted(String method) throws Exception {
        submit(method, valid().without("description"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void goalCreatedWithMaxDescription_canBeUpdatedWithSameDescription() throws Exception {
        // Given a goal created with a maximum-length description
        String description = "a".repeat(DESCRIPTION_MAX);
        String response = createGoal(USER, valid().with("description", description).json())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(response, "$.id")).longValue();

        // When it is updated with the same description
        mockMvc.perform(put("/api/goal/{id}", id)
                        .header(AUTHORIZATION, authHeader(USER))
                        .contentType(APPLICATION_JSON)
                        .content(valid().with("description", description).json()))

                // Then the update succeeds
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(description));
    }

    private ResultActions submit(String method, GoalPayload payload) throws Exception {
        if (method.equals("POST")) {
            return createGoal(USER, payload.json());
        }
        long id = createGoalId(USER);
        return mockMvc.perform(put("/api/goal/{id}", id)
                .header(AUTHORIZATION, authHeader(USER))
                .contentType(APPLICATION_JSON)
                .content(payload.json()));
    }
}
