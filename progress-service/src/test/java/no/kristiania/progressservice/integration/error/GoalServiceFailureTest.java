package no.kristiania.progressservice.integration.error;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import no.kristiania.progressservice.integration.setup.AbstractProgressApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class GoalServiceFailureTest extends AbstractProgressApiTest {

    private static final long USER = 1L;
    private static final long GOAL_ID = 10L;

    @Test
    void post_goalServiceDown_returns503() throws Exception {
        when(goalClient.getGoal(GOAL_ID)).thenThrow(new ResourceAccessException("Connection refused"));

        postProgress(USER, GOAL_ID, TODAY).andExpect(status().isServiceUnavailable());
    }

    @Test
    void post_goalServiceError_returns503() throws Exception {
        when(goalClient.getGoal(GOAL_ID)).thenThrow(HttpServerErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", new HttpHeaders(), null, null));

        postProgress(USER, GOAL_ID, TODAY).andExpect(status().isServiceUnavailable());
    }

    @Test
    void post_goalServiceDown_savesNothing() throws Exception {
        when(goalClient.getGoal(GOAL_ID)).thenThrow(new ResourceAccessException("Connection refused"));

        postProgress(USER, GOAL_ID, TODAY);

        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "progress")).isZero();
    }

    @Test
    void post_goalNotFound_returns404() throws Exception {
        when(goalClient.getGoal(GOAL_ID)).thenThrow(HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", new HttpHeaders(), null, null));

        postProgress(USER, GOAL_ID, TODAY).andExpect(status().isNotFound());
    }

    @Test
    void post_goalServiceRejectsRequest_returns503() throws Exception {
        when(goalClient.getGoal(GOAL_ID)).thenThrow(HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED, "Unauthorized", new HttpHeaders(), null, null));

        postProgress(USER, GOAL_ID, TODAY).andExpect(status().isServiceUnavailable());
    }

    @Test
    void post_goalServiceKeepsFailing_opensBreakerAndStopsCallingGoal() throws Exception {
        when(goalClient.getGoal(GOAL_ID)).thenThrow(new ResourceAccessException("Connection refused"));

        for (int i = 0; i < 6; i++) {
            postProgress(USER, GOAL_ID, TODAY).andExpect(status().isServiceUnavailable());
        }

        verify(goalClient, times(5)).getGoal(GOAL_ID);
        assertThat(goalCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void post_goalNotFoundManyTimes_keepsBreakerClosed() throws Exception {
        when(goalClient.getGoal(GOAL_ID)).thenThrow(HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", new HttpHeaders(), null, null));

        for (int i = 0; i < 6; i++) {
            postProgress(USER, GOAL_ID, TODAY).andExpect(status().isNotFound());
        }

        verify(goalClient, times(6)).getGoal(GOAL_ID);
        assertThat(goalCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void post_archivedGoal_returns409() throws Exception {
        goalHasStatus(GOAL_ID, "ARCHIVED");

        postProgress(USER, GOAL_ID, TODAY).andExpect(status().isConflict());
    }

    @Test
    void post_activeGoal_returns201() throws Exception {
        goalHasStatus(GOAL_ID, "ACTIVE");

        postProgress(USER, GOAL_ID, TODAY).andExpect(status().isCreated());
    }
}
