package no.kristiania.goalservice.integration.persistence;

import no.kristiania.goalservice.goal.Goal;
import no.kristiania.goalservice.goal.GoalRepository;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import no.kristiania.goalservice.goal.enums.GoalStatus;
import no.kristiania.goalservice.goal.enums.GoalType;
import no.kristiania.goalservice.goal.enums.TrackingType;
import no.kristiania.goalservice.integration.setup.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

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

    private static final String INSERT_TYPES = """
            INSERT INTO goals (user_id, title, period, times_per_period, goal_type, tracking_type,
                               created_at, updated_at)
            VALUES (?, 'Run', 'DAILY', 1, ?, ?, now(), now())""";

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

    private Long insertCategory() {
        String code = "T_" + UUID.randomUUID().toString().substring(0, 8);
        return jdbcTemplate.queryForObject(
                "INSERT INTO categories (code, name) VALUES (?, 'Test') RETURNING id", Long.class, code);
    }

    private Long insertTemplate(Long categoryId) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO goal_templates (category_id, title, goal_type, tracking_type)
                VALUES (?, 'Template', 'HABIT', 'CHECK') RETURNING id""", Long.class, categoryId);
    }

    // ---------- timestamps, versioning and queries ----------

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

    // ---------- new columns: mapping and defaults ----------

    @Test
    void save_allNewFields_areReadBackUnchanged() {
        Long categoryId = insertCategory();
        Long templateId = insertTemplate(categoryId);
        Goal goal = newGoal("Marathon");
        goal.setGoalType(GoalType.EVENT);
        goal.setTrackingType(TrackingType.EVENT);
        goal.setTargetTime(LocalTime.of(22, 30));
        goal.setStartDate(LocalDate.of(2026, 4, 1));
        goal.setTargetDate(LocalDate.of(2026, 9, 12));
        goal.setMotivation("Feel stronger");
        goal.setIconKey("run");
        goal.setColor("#FF5722");
        goal.setCategoryId(categoryId);
        goal.setTemplateId(templateId);
        Long id = goalRepository.saveAndFlush(goal).getId();

        Goal reloaded = goalRepository.findById(id).orElseThrow();

        assertThat(reloaded.getGoalType()).isEqualTo(GoalType.EVENT);
        assertThat(reloaded.getTrackingType()).isEqualTo(TrackingType.EVENT);
        assertThat(reloaded.getTargetTime()).isEqualTo(LocalTime.of(22, 30));
        assertThat(reloaded.getStartDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(reloaded.getTargetDate()).isEqualTo(LocalDate.of(2026, 9, 12));
        assertThat(reloaded.getMotivation()).isEqualTo("Feel stronger");
        assertThat(reloaded.getIconKey()).isEqualTo("run");
        assertThat(reloaded.getColor()).isEqualTo("#FF5722");
        assertThat(reloaded.getCategoryId()).isEqualTo(categoryId);
        assertThat(reloaded.getTemplateId()).isEqualTo(templateId);
    }

    @Test
    void save_enums_areStoredAsStrings() {
        Goal goal = newGoal("Water");
        goal.setGoalType(GoalType.TARGET);
        goal.setTrackingType(TrackingType.AMOUNT);
        Long id = goalRepository.saveAndFlush(goal).getId();

        String goalType = jdbcTemplate.queryForObject(
                "SELECT goal_type FROM goals WHERE id = ?", String.class, id);
        String trackingType = jdbcTemplate.queryForObject(
                "SELECT tracking_type FROM goals WHERE id = ?", String.class, id);

        assertThat(goalType).isEqualTo("TARGET");
        assertThat(trackingType).isEqualTo("AMOUNT");
    }

    @Test
    void save_withNullPeriodAndTimes_isAccepted() {
        Goal goal = newGoal("Meditate");
        goal.setTrackingType(TrackingType.CHECK);
        goal.setPeriod(null);
        goal.setTimesPerPeriod(null);

        Goal reloaded = goalRepository.findById(goalRepository.saveAndFlush(goal).getId()).orElseThrow();

        assertThat(reloaded.getPeriod()).isNull();
        assertThat(reloaded.getTimesPerPeriod()).isNull();
    }

    @Test
    void insertRaw_withoutTypes_getsDatabaseDefaults() {
        jdbcTemplate.update("""
                INSERT INTO goals (user_id, title, period, times_per_period, created_at, updated_at)
                VALUES (?, 'Run', 'DAILY', 1, now(), now())""", USER);

        String goalType = jdbcTemplate.queryForObject("SELECT goal_type FROM goals", String.class);
        String trackingType = jdbcTemplate.queryForObject("SELECT tracking_type FROM goals", String.class);

        assertThat(goalType).isEqualTo("HABIT");
        assertThat(trackingType).isEqualTo("COUNT");
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

    // ---------- check constraints ----------

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
    @ValueSource(strings = {"HABIT", "TARGET", "EVENT", "OTHER"})
    void insertRaw_validGoalType_isAccepted(String goalType) {
        assertThatCode(() -> jdbcTemplate.update(INSERT_TYPES, USER, goalType, "COUNT"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GOAL", "habit", ""})
    void insertRaw_invalidGoalType_isRejectedByCheck(String goalType) {
        assertThatThrownBy(() -> jdbcTemplate.update(INSERT_TYPES, USER, goalType, "COUNT"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_goals_goal_type");
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHECK", "COUNT", "AMOUNT", "DURATION", "TIME_TARGET", "LIMIT", "EVENT"})
    void insertRaw_validTrackingType_isAccepted(String trackingType) {
        assertThatCode(() -> jdbcTemplate.update(INSERT_TYPES, USER, "HABIT", trackingType))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SLEEP", "count", ""})
    void insertRaw_invalidTrackingType_isRejectedByCheck(String trackingType) {
        assertThatThrownBy(() -> jdbcTemplate.update(INSERT_TYPES, USER, "HABIT", trackingType))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_goals_tracking_type");
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
    void insertRaw_targetDateBeforeStartDate_isRejectedByCheck() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO goals (user_id, title, period, times_per_period, start_date, target_date,
                                   created_at, updated_at)
                VALUES (?, 'Run', 'DAILY', 1, ?, ?, now(), now())""",
                USER, Date.valueOf("2026-06-02"), Date.valueOf("2026-06-01")))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_goals_dates");
    }

    @Test
    void insertRaw_targetDateSameAsStartDate_isAccepted() {
        assertThatCode(() -> jdbcTemplate.update("""
                INSERT INTO goals (user_id, title, period, times_per_period, start_date, target_date,
                                   created_at, updated_at)
                VALUES (?, 'Run', 'DAILY', 1, ?, ?, now(), now())""",
                USER, Date.valueOf("2026-06-01"), Date.valueOf("2026-06-01")))
                .doesNotThrowAnyException();
    }

    // ---------- foreign keys ----------

    @Test
    void save_unknownCategoryId_isRejectedByForeignKey() {
        Goal goal = newGoal("Run");
        goal.setCategoryId(999_999L);

        assertThatThrownBy(() -> goalRepository.saveAndFlush(goal))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fk_goals_category");
    }

    @Test
    void save_unknownTemplateId_isRejectedByForeignKey() {
        Goal goal = newGoal("Run");
        goal.setTemplateId(999_999L);

        assertThatThrownBy(() -> goalRepository.saveAndFlush(goal))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fk_goals_template");
    }

    // ---------- lengths ----------

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

    @ParameterizedTest
    @CsvSource({"motivation,500", "icon_key,50", "color,7"})
    void newTextColumns_atMaxLength_areAccepted(String column, int max) {
        Long id = goalRepository.saveAndFlush(newGoal("Run")).getId();

        assertThatCode(() -> jdbcTemplate.update(
                "UPDATE goals SET " + column + " = ? WHERE id = ?", "x".repeat(max), id))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({"motivation,500", "icon_key,50", "color,7"})
    void newTextColumns_overMaxLength_areRejected(String column, int max) {
        Long id = goalRepository.saveAndFlush(newGoal("Run")).getId();

        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE goals SET " + column + " = ? WHERE id = ?", "x".repeat(max + 1), id))
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

    // ---------- nullability ----------

    @ParameterizedTest
    @ValueSource(strings = {"user_id", "title", "status", "version", "created_at", "updated_at",
            "goal_type", "tracking_type"})
    void setRequiredColumnToNull_isRejected(String column) {
        Long id = goalRepository.saveAndFlush(newGoal("Run")).getId();

        assertThatThrownBy(() ->
                jdbcTemplate.update("UPDATE goals SET " + column + " = NULL WHERE id = ?", id))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"description", "period", "times_per_period", "amount_per_session", "unit",
            "template_id", "category_id", "target_time", "start_date", "target_date",
            "motivation", "icon_key", "color", "archived_at"})
    void setOptionalColumnToNull_isAccepted(String column) {
        Long id = goalRepository.saveAndFlush(newGoal("Run")).getId();

        assertThatCode(() ->
                jdbcTemplate.update("UPDATE goals SET " + column + " = NULL WHERE id = ?", id))
                .doesNotThrowAnyException();
    }
}