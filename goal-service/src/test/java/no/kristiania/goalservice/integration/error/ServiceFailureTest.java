package no.kristiania.goalservice.integration.error;

import no.kristiania.goalservice.goal.Goal;
import no.kristiania.goalservice.goal.GoalRepository;
import no.kristiania.goalservice.integration.setup.AbstractGoalApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ServiceFailureTest extends AbstractGoalApiTest {

    private static final long USER = 1L;

    @MockitoBean GoalRepository GoalRepository;

    @Test
    void unexpectedException_returns500_withoutLeakingDetails() throws Exception {
        when(GoalRepository.findByIdAndUserId(any(), any())).thenThrow(new RuntimeException("db password is hunter2"));

        getGoal(USER, 1L)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Unexpected error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void optimisticLockFailure_returns409_withFixedMessage() throws Exception {
        when(GoalRepository.findByIdAndUserId(any(), any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Goal.class, 1L));

        getGoal(USER, 1L)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("The resource was modified by another request. Reload and try again"));
    }
}
