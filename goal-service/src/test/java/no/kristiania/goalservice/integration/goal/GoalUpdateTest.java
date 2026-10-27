package no.kristiania.goalservice.integration.goal;

import com.jayway.jsonpath.JsonPath;
import no.kristiania.goalservice.integration.setup.AbstractGoalApiTest;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static no.kristiania.goalservice.integration.setup.GoalPayload.valid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GoalUpdateTest extends AbstractGoalApiTest {

    private static final long USER = 1L;

    @Test
    void update_replacesAllFields() throws Exception {
        long id = createGoalId(USER);

        updateGoal(USER, id, valid()
                .with("title", "Run more").with("description", "Evening runs")
                .with("period", "DAILY").with("timesPerPeriod", 1)
                .with("amountPerSession", 10.0).with("unit", "miles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Run more"))
                .andExpect(jsonPath("$.description").value("Evening runs"))
                .andExpect(jsonPath("$.period").value("DAILY"))
                .andExpect(jsonPath("$.timesPerPeriod").value(1))
                .andExpect(jsonPath("$.amountPerSession").value(10.0))
                .andExpect(jsonPath("$.unit").value("miles"));
        getGoal(USER, id).andExpect(jsonPath("$.title").value("Run more"));
    }

    @Test
    void update_withoutOptionalFields_clearsThem() throws Exception {
        long id = createGoalId(USER);

        updateGoal(USER, id, valid().without("description").without("amountPerSession").without("unit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(nullValue()))
                .andExpect(jsonPath("$.amountPerSession").value(nullValue()))
                .andExpect(jsonPath("$.unit").value(nullValue()));
    }

    @Test
    void update_increasesVersion_andUpdatedAt() throws Exception {
        String created = bodyOf(createGoal(USER, valid()).andExpect(status().isCreated()));
        long id = ((Number) JsonPath.read(created, "$.id")).longValue();
        Instant createdAt = Instant.parse(JsonPath.read(created, "$.createdAt"));

        String updated = bodyOf(updateGoal(USER, id, valid().with("title", "Changed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1)));

        Instant updatedAt = Instant.parse(JsonPath.read(updated, "$.updatedAt"));
        assertThat(updatedAt).isAfter(createdAt);
    }

    @Test
    void update_withIdenticalValues_keepsVersion() throws Exception {
        long id = createGoalId(USER, valid());

        updateGoal(USER, id, valid())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    void update_ignoresStatusUserIdAndCreatedAtInBody() throws Exception {
        String created = bodyOf(createGoal(USER, valid()).andExpect(status().isCreated()));
        long id = ((Number) JsonPath.read(created, "$.id")).longValue();
        String createdAt = JsonPath.read(created, "$.createdAt");

        updateGoal(USER, id, valid()
                .with("status", "ARCHIVED").with("userId", 99).with("createdAt", "2000-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").value(createdAt));
        getGoal(USER, id).andExpect(status().isOk());
    }

    @Test
    void update_archivedGoal_returns409_andLeavesGoalUnchanged() throws Exception {
        long id = createGoalId(USER, valid().with("title", "Original"));
        archiveGoal(USER, id).andExpect(status().isOk());

        updateGoal(USER, id, valid().with("title", "Changed"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Archived goal cannot be updated"));
        getGoal(USER, id).andExpect(jsonPath("$.title").value("Original"));
    }

    @Test
    void update_unknownId_returns404() throws Exception {
        updateGoal(USER, 999_999L, valid())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal with id 999999 not found"));
    }

    @Test
    void update_worksAgain_afterGoalIsUnarchived() throws Exception {
        long id = createGoalId(USER, valid().with("title", "Original"));
        archiveGoal(USER, id).andExpect(status().isOk());
        unarchiveGoal(USER, id).andExpect(status().isOk());

        updateGoal(USER, id, valid().with("title", "Changed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Changed"));
    }
}
