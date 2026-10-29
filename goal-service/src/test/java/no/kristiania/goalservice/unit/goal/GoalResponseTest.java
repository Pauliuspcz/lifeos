package no.kristiania.goalservice.unit.goal;

import no.kristiania.goalservice.goal.Goal;
import no.kristiania.goalservice.goal.dto.GoalResponse;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import no.kristiania.goalservice.goal.enums.GoalStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class GoalResponseTest {

    private static final Instant CREATED = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant UPDATED = Instant.parse("2026-02-02T11:00:00Z");
    private static final Instant ARCHIVED = Instant.parse("2026-03-03T12:00:00Z");

    @Test
    void from_mapsEveryField() {
        // Given a goal where every field has a different value
        Goal goal = new Goal();
        goal.setId(5L);
        goal.setUserId(99L);
        goal.setTitle("Run");
        goal.setDescription("Morning");
        goal.setPeriod(GoalPeriod.MONTHLY);
        goal.setTimesPerPeriod(3);
        goal.setAmountPerSession(new BigDecimal("5.50"));
        goal.setUnit("km");
        goal.setStatus(GoalStatus.ARCHIVED);
        goal.setTemplateId(8L);
        goal.setVersion(4L);
        goal.setCreatedAt(CREATED);
        goal.setUpdatedAt(UPDATED);
        goal.setArchivedAt(ARCHIVED);

        // When
        GoalResponse response = GoalResponse.from(goal);

        // Then each value ends up in the matching field
        assertThat(response).isEqualTo(new GoalResponse(
                5L, "Run", "Morning", GoalPeriod.MONTHLY, 3, new BigDecimal("5.50"), "km",
                GoalStatus.ARCHIVED, 8L, 4L, CREATED, UPDATED, ARCHIVED));
    }

    @Test
    void from_optionalFieldsNull_mapsToNull() {
        // Given a goal with only required fields set
        Goal goal = new Goal();
        goal.setId(5L);
        goal.setUserId(99L);
        goal.setTitle("Run");
        goal.setPeriod(GoalPeriod.DAILY);
        goal.setTimesPerPeriod(1);
        goal.setVersion(0L);
        goal.setCreatedAt(CREATED);
        goal.setUpdatedAt(CREATED);

        // When
        GoalResponse response = GoalResponse.from(goal);

        // Then the optional fields are null, and status keeps its default
        assertThat(response.description()).isNull();
        assertThat(response.amountPerSession()).isNull();
        assertThat(response.unit()).isNull();
        assertThat(response.templateId()).isNull();
        assertThat(response.archivedAt()).isNull();
        assertThat(response.status()).isEqualTo(GoalStatus.ACTIVE);
    }

    @Test
    void response_doesNotExposeUserId() {
        String[] fields = Arrays.stream(GoalResponse.class.getRecordComponents())
                .map(RecordComponent::getName)
                .toArray(String[]::new);

        assertThat(fields).doesNotContain("userId");
    }
}
