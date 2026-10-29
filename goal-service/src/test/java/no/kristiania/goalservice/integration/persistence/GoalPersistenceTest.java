package no.kristiania.goalservice.integration.persistence;

import no.kristiania.goalservice.goal.Goal;
import no.kristiania.goalservice.goal.GoalRepository;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import no.kristiania.goalservice.goal.enums.GoalStatus;
import no.kristiania.goalservice.integration.setup.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class GoalPersistenceTest extends AbstractIntegrationTest {

    private static final Long USER = 1L;
    private static final Long OTHER_USER = 2L;

    private static final String INSERT_RAW = """
            INSERT INTO goals (user_id, title, description, period, times_per_period,
                               amount_per_session, unit, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, now(), now())""";

    @Autowired
    GoalRepository goalRepository;

    private Goal newGoal(String title) {
        Goal goal = new Goal();
        goal.setUserId(USER);
        goal.setTitle(title);
        goal.setPeriod(GoalPeriod.WEEKLY);
        goal.setTimesPerPeriod(3);
        return goal;
    }

    private void insertRaw(String title, String description, String period,
                           int times, BigDecimal amount, String unit, String status) {
        jdbcTemplate.update(INSERT_RAW, USER, title, description, period, times, amount, unit, status);
    }

    @Test
    void save_setsCreatedAtAndUpdatedAt_toTheSameValue() {
        Goal saved = goalRepository.saveAndFlush(newGoal("Run"));

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    void update_changesUpdatedAt_butNotCreatedAt() {
        Goal saved = goalRepository.saveAndFlush(newGoal("Run"));
        Instant createdAt = saved.getCreatedAt();

        saved.setTitle("Run more");
        Goal updated = goalRepository.saveAndFlush(saved);

        assertThat(updated.getUpdatedAt()).isAfter(createdAt);
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void update_ignoresChangesToUserIdAndCreatedAt() {
        Goal saved = goalRepository.saveAndFlush(newGoal("Run"));
        Instant originalCreatedAt = goalRepository.findById(saved.getId()).orElseThrow().getCreatedAt();

        Goal detached = goalRepository.findById(saved.getId()).orElseThrow();
        detached.setUserId(OTHER_USER);
        detached.setCreatedAt(Instant.parse("2000-01-01T00:00:00Z"));
        detached.setTitle("Run more");
        goalRepository.saveAndFlush(detached);

        Goal reloaded = goalRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("Run more");
        assertThat(reloaded.getUserId()).isEqualTo(USER);
        assertThat(reloaded.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    void update_withStaleVersion_throwsOptimisticLockingFailure() {
        Long id = goalRepository.saveAndFlush(newGoal("Run")).getId();
        Goal first = goalRepository.findById(id).orElseThrow();
        Goal second = goalRepository.findById(id).orElseThrow();

        first.setTitle("First");
        goalRepository.saveAndFlush(first);
        second.setTitle("Second");

        assertThatThrownBy(() -> goalRepository.saveAndFlush(second))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(goalRepository.findById(id).orElseThrow().getTitle()).isEqualTo("First");
    }

    @Test
    void findByIdAndUserId_ownGoal_returnsIt() {
        Long id = goalRepository.saveAndFlush(newGoal("Run")).getId();

        assertThat(goalRepository.findByIdAndUserId(id, USER)).isPresent();
    }

    @Test
    void findByIdAndUserId_otherUser_returnsEmpty() {
        Long id = goalRepository.saveAndFlush(newGoal("Run")).getId();

        assertThat(goalRepository.findByIdAndUserId(id, OTHER_USER)).isEmpty();
    }

    @Test
    void findAllByUser_returnsOnlyOwnGoals_newestFirst() {
        goalRepository.saveAndFlush(newGoal("first"));
        goalRepository.saveAndFlush(newGoal("second"));
        goalRepository.saveAndFlush(newGoal("third"));
        Goal foreign = newGoal("foreign");
        foreign.setUserId(OTHER_USER);
        goalRepository.saveAndFlush(foreign);

        List<Goal> result = goalRepository.findAllByUserIdOrderByCreatedAtDesc(USER);

        assertThat(result).extracting(Goal::getTitle).containsExactly("third", "second", "first");
    }

    @Test
    void findAllByUserAndStatus_returnsOnlyThatStatus() {
        goalRepository.saveAndFlush(newGoal("active"));
        Goal archived = newGoal("archived");
        archived.setStatus(GoalStatus.ARCHIVED);
        goalRepository.saveAndFlush(archived);

        List<Goal> result = goalRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(USER, GoalStatus.ARCHIVED);

        assertThat(result).extracting(Goal::getTitle).containsExactly("archived");
    }

    @Test
    void timestamps_readBackExactlyAsSaved() {
        Goal goal = newGoal("Run");
        goal.setStatus(GoalStatus.ARCHIVED);
        goal.setArchivedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        Goal saved = goalRepository.saveAndFlush(goal);

        Goal reloaded = goalRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getCreatedAt()).isEqualTo(saved.getCreatedAt());
        assertThat(reloaded.getUpdatedAt()).isEqualTo(saved.getUpdatedAt());
        assertThat(reloaded.getArchivedAt()).isEqualTo(saved.getArchivedAt());
    }

    @Test
    void createdAt_isCloseToNow() {
        Goal saved = goalRepository.saveAndFlush(newGoal("Run"));

        Goal reloaded = goalRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getCreatedAt()).isCloseTo(Instant.now(), within(10, ChronoUnit.SECONDS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DAILY", "WEEKLY", "MONTHLY", "YEARLY"})
    void insertRaw_validPeriod_isAccepted(String period) {
        assertThatCode(() -> insertRaw("Run", null, period, 1, null, null, "ACTIVE"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"HOURLY", "daily", ""})
    void insertRaw_invalidPeriod_isRejectedByCheck(String period) {
        assertThatThrownBy(() -> insertRaw("Run", null, period, 1, null, null, "ACTIVE"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_goals_period");
    }

    @Test
    void insertRaw_invalidStatus_isRejectedByCheck() {
        assertThatThrownBy(() -> insertRaw("Run", null, "DAILY", 1, null, null, "DELETED"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_goals_status");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void insertRaw_timesPerPeriodBelowOne_isRejectedByCheck(int times) {
        assertThatThrownBy(() -> insertRaw("Run", null, "DAILY", times, null, null, "ACTIVE"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_goals_times_per_period");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-0.01"})
    void insertRaw_amountZeroOrNegative_isRejectedByCheck(String amount) {
        assertThatThrownBy(() -> insertRaw("Run", null, "DAILY", 1, new BigDecimal(amount), null, "ACTIVE"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_goals_amount");
    }

    @Test
    void insertRaw_smallestPositiveAmount_isAccepted() {
        assertThatCode(() -> insertRaw("Run", null, "DAILY", 1, new BigDecimal("0.01"), null, "ACTIVE"))
                .doesNotThrowAnyException();
    }

    @Test
    void insertRaw_titleOf255Chars_isAccepted() {
        assertThatCode(() -> insertRaw("x".repeat(255), null, "DAILY", 1, null, null, "ACTIVE"))
                .doesNotThrowAnyException();
    }

    @Test
    void insertRaw_titleOf256Chars_isRejected() {
        assertThatThrownBy(() -> insertRaw("x".repeat(256), null, "DAILY", 1, null, null, "ACTIVE"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void insertRaw_descriptionOf1000Chars_isAccepted() {
        assertThatCode(() -> insertRaw("Run", "x".repeat(1000), "DAILY", 1, null, null, "ACTIVE"))
                .doesNotThrowAnyException();
    }

    @Test
    void insertRaw_descriptionOf1001Chars_isRejected() {
        assertThatThrownBy(() -> insertRaw("Run", "x".repeat(1001), "DAILY", 1, null, null, "ACTIVE"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void insertRaw_unitOf50Chars_isAccepted() {
        assertThatCode(() -> insertRaw("Run", null, "DAILY", 1, null, "x".repeat(50), "ACTIVE"))
                .doesNotThrowAnyException();
    }

    @Test
    void insertRaw_unitOf51Chars_isRejected() {
        assertThatThrownBy(() -> insertRaw("Run", null, "DAILY", 1, null, "x".repeat(51), "ACTIVE"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void insertRaw_largestAmount_isAccepted() {
        assertThatCode(() -> insertRaw("Run", null, "DAILY", 1, new BigDecimal("99999999.99"), null, "ACTIVE"))
                .doesNotThrowAnyException();
    }

    @Test
    void insertRaw_amountOf100000000_isRejected() {
        assertThatThrownBy(() -> insertRaw("Run", null, "DAILY", 1, new BigDecimal("100000000"), null, "ACTIVE"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"user_id", "title", "period", "times_per_period",
            "status", "version", "created_at", "updated_at"})
    void setRequiredColumnToNull_isRejected(String column) {
        Long id = goalRepository.saveAndFlush(newGoal("Run")).getId();

        assertThatThrownBy(() ->
                jdbcTemplate.update("UPDATE goals SET " + column + " = NULL WHERE id = ?", id))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void insertRaw_withoutStatusAndVersion_getsDatabaseDefaults() {
        jdbcTemplate.update("""
                INSERT INTO goals (user_id, title, period, times_per_period, created_at, updated_at)
                VALUES (?, 'Run', 'DAILY', 1, now(), now())""", USER);

        String status = jdbcTemplate.queryForObject("SELECT status FROM goals", String.class);
        Long version = jdbcTemplate.queryForObject("SELECT version FROM goals", Long.class);

        assertThat(status).isEqualTo("ACTIVE");
        assertThat(version).isZero();
    }
}