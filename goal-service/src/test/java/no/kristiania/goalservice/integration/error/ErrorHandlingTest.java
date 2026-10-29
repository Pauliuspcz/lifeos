package no.kristiania.goalservice.integration.error;

import no.kristiania.goalservice.integration.setup.AbstractGoalApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static no.kristiania.goalservice.integration.setup.GoalPayload.valid;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ErrorHandlingTest extends AbstractGoalApiTest {

    private static final long USER = 1L;

    private void expectError(ResultActions result, int status, String error, String message) throws Exception {
        result.andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.error").value(error))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getUnknownGoal_returns404_withMessage() throws Exception {
        expectError(getGoal(USER, 999L), 404, "Not Found", "Goal with id 999 not found");
    }

    @Test
    void createWithBlankTitle_returns400_withFieldError() throws Exception {
        ResultActions result = createGoal(USER, valid().with("title", ""));

        expectError(result, 400, "Bad Request", "Validation failed");
        result.andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    void createWithMalformedJson_returns400_withoutFieldErrors() throws Exception {
        ResultActions result = createGoal(USER, "{");

        expectError(result, 400, "Bad Request", "Request body is missing or malformed");
        result.andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void getWithNonNumericId_returns400_namingTheParameter() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/goal/abc").header(AUTHORIZATION, authHeader(USER)));

        expectError(result, 400, "Bad Request", "Invalid value for parameter 'id'");
    }

    @Test
    void updateArchivedGoal_returns409_withMessage() throws Exception {
        long id = createGoalId(USER);
        archiveGoal(USER, id).andExpect(status().isOk());

        expectError(updateGoal(USER, id, valid()), 409, "Conflict", "Archived goal cannot be updated");
    }

    @Test
    void deleteActiveGoal_returns409_withMessage() throws Exception {
        long id = createGoalId(USER);

        expectError(deleteGoal(USER, id), 409, "Conflict", "Goal must be archived before it can be deleted");
    }

    @Test
    void nonValidationError_hasNoFieldErrorsProperty() throws Exception {
        getGoal(USER, 999L).andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void unsupportedMethod_returns405_inApiErrorFormat() throws Exception {
        ResultActions result = mockMvc.perform(delete("/api/goal").header(AUTHORIZATION, authHeader(USER)));

        expectError(result, 405, "Method Not Allowed", "HTTP method not supported");
        result.andExpect(header().exists("Allow"));
    }

    @Test
    void unsupportedContentType_returns415_inApiErrorFormat() throws Exception {
        ResultActions result = mockMvc.perform(post("/api/goal")
                .header(AUTHORIZATION, authHeader(USER))
                .contentType(MediaType.TEXT_PLAIN).content("hello"));

        expectError(result, 415, "Unsupported Media Type", "Content type not supported");
    }

    @Test
    void unknownRoute_returns404_inApiErrorFormat() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/nothing").header(AUTHORIZATION, authHeader(USER)));

        expectError(result, 404, "Not Found", "Resource not found");
    }
}
