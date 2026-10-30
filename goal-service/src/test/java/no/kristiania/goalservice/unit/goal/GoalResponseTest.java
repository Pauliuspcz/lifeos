package no.kristiania.goalservice.unit.goal;

import no.kristiania.goalservice.goal.Goal;
import no.kristiania.goalservice.goal.dto.GoalResponse;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import no.kristiania.goalservice.goal.enums.GoalStatus;
import no.kristiania.goalservice.goal.enums.GoalType;
import no.kristiania.goalservice.goal.enums.TrackingType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class GoalResponseTest {

    private static final Instant CREATED = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant UPDATED = Instant.parse("2026-02-02T11:00:00Z");
    private static final Instant ARCHIVED = Instant.parse("2026-03-03T12:00:00Z");
    private static final LocalTime TARGET_TIME = LocalTime.of(22, 30);
    private static final LocalDate START = LocalDate.of(2026, 4, 1);
    private static final LocalDate TARGET = LocalDate.of(2026, 9, 12);

    @Test
    void from_mapsEveryField() {
        // Given a goal where every field has a different value
        Goal goal = new Goal();
        goal.setId(5L);
        goal.setUserId(99L);
        goal.setTitle("Run");
        goal.setDescription("Morning");
        goal.setGoalType(GoalType.EVENT);
        goal.setTrackingType(TrackingType.AMOUNT);
        goal.setPeriod(GoalPeriod.MONTHLY);
        goal.setTimesPerPeriod(3);
        goal.setAmountPerSession(new BigDecimal("5.50"));
        goal.setUnit("km");
        goal.setTargetTime(TARGET_TIME);
        goal.setStartDate(START);
        goal.setTargetDate(TARGET);
        goal.setMotivation("Feel stronger");
        goal.setIconKey("run");
        goal.setColor("#FF5722");
        goal.setCategoryId(7L);
        goal.setTemplateId(8L);
        goal.setStatus(GoalStatus.ARCHIVED);
        goal.setVersion(4L);
        goal.setCreatedAt(CREATED);
        goal.setUpdatedAt(UPDATED);
        goal.setArchivedAt(ARCHIVED);

        // When
        GoalResponse response = GoalResponse.from(goal);

        // Then each value ends up in the matching field
        assertThat(response).isEqualTo(new GoalResponse(
                5L, "Run", "Morning", GoalType.EVENT, TrackingType.AMOUNT,
                GoalPeriod.MONTHLY, 3, new BigDecimal("5.50"), "km",
                TARGET_TIME, START, TARGET, "Feel stronger", "run", "#FF5722",
                7L, 8L, GoalStatus.ARCHIVED, 4L, CREATED, UPDATED, ARCHIVED));
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

        // Then the optional fields are null, and enums keep their defaults
        assertThat(response.description()).isNull();
        assertThat(response.amountPerSession()).isNull();
        assertThat(response.unit()).isNull();
        assertThat(response.targetTime()).isNull();
        assertThat(response.startDate()).isNull();
        assertThat(response.targetDate()).isNull();
        assertThat(response.motivation()).isNull();
        assertThat(response.iconKey()).isNull();
        assertThat(response.color()).isNull();
        assertThat(response.categoryId()).isNull();
        assertThat(response.templateId()).isNull();
        assertThat(response.archivedAt()).isNull();
        assertThat(response.status()).isEqualTo(GoalStatus.ACTIVE);
        assertThat(response.goalType()).isEqualTo(GoalType.HABIT);
        assertThat(response.trackingType()).isEqualTo(TrackingType.COUNT);
    }

    @Test
    void from_periodAndTimesPerPeriodNull_doesNotThrow() {
        // Given a CHECK goal without period and frequency
        Goal goal = new Goal();
        goal.setId(6L);
        goal.setUserId(99L);
        goal.setTitle("Meditate");
        goal.setTrackingType(TrackingType.CHECK);
        goal.setVersion(0L);
        goal.setCreatedAt(CREATED);
        goal.setUpdatedAt(CREATED);

        // When
        GoalResponse response = GoalResponse.from(goal);

        // Then null maps through instead of causing an unboxing NPE
        assertThat(response.period()).isNull();
        assertThat(response.timesPerPeriod()).isNull();
        assertThat(response.trackingType()).isEqualTo(TrackingType.CHECK);
    }

    @Test
    void response_doesNotExposeUserId() {
        String[] fields = Arrays.stream(GoalResponse.class.getRecordComponents())
                .map(RecordComponent::getName)
                .toArray(String[]::new);

        assertThat(fields).doesNotContain("userId");
    }
}