package no.kristiania.goalservice.unit.goal;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import no.kristiania.goalservice.goal.dto.CreateGoalRequest;
import no.kristiania.goalservice.goal.dto.UpdateGoalRequest;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class GoalRequestValidationTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static final String TITLE = "Run";
    private static final String DESCRIPTION = "Morning";
    private static final GoalPeriod PERIOD = GoalPeriod.WEEKLY;
    private static final Integer TIMES = 3;
    private static final BigDecimal AMOUNT = new BigDecimal("5.0");
    private static final String UNIT = "km";

    private static CreateGoalRequest valid() {
        return new CreateGoalRequest(TITLE, DESCRIPTION, PERIOD, TIMES, AMOUNT, UNIT);
    }

    private static CreateGoalRequest withTitle(String v) { return new CreateGoalRequest(v, DESCRIPTION, PERIOD, TIMES, AMOUNT, UNIT); }
    private static CreateGoalRequest withDescription(String v) { return new CreateGoalRequest(TITLE, v, PERIOD, TIMES, AMOUNT, UNIT); }
    private static CreateGoalRequest withPeriod(GoalPeriod v) { return new CreateGoalRequest(TITLE, DESCRIPTION, v, TIMES, AMOUNT, UNIT); }
    private static CreateGoalRequest withTimes(Integer v) { return new CreateGoalRequest(TITLE, DESCRIPTION, PERIOD, v, AMOUNT, UNIT); }
    private static CreateGoalRequest withAmount(BigDecimal v) { return new CreateGoalRequest(TITLE, DESCRIPTION, PERIOD, TIMES, v, UNIT); }
    private static CreateGoalRequest withUnit(String v) { return new CreateGoalRequest(TITLE, DESCRIPTION, PERIOD, TIMES, AMOUNT, v); }

    private static Set<String> violatedFields(Object request) {
        return VALIDATOR.validate(request).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(violatedFields(valid())).isEmpty();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void title_nullOrBlank_isRejected(String title) {
        assertThat(violatedFields(withTitle(title))).containsExactly("title");
    }

    @Test
    void title_of255Chars_isAccepted() {
        assertThat(violatedFields(withTitle("x".repeat(255)))).isEmpty();
    }

    @Test
    void title_of256Chars_isRejected() {
        assertThat(violatedFields(withTitle("x".repeat(256)))).containsExactly("title");
    }

    @Test
    void description_null_isAccepted() {
        assertThat(violatedFields(withDescription(null))).isEmpty();
    }

    @Test
    void description_of1000Chars_isAccepted() {
        assertThat(violatedFields(withDescription("x".repeat(1000)))).isEmpty();
    }

    @Test
    void description_of1001Chars_isRejected() {
        assertThat(violatedFields(withDescription("x".repeat(1001)))).containsExactly("description");
    }

    @Test
    void period_null_isRejected() {
        assertThat(violatedFields(withPeriod(null))).containsExactly("period");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 1000})
    void timesPerPeriod_oneOrMore_isAccepted(int times) {
        assertThat(violatedFields(withTimes(times))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void timesPerPeriod_belowOne_isRejected(int times) {
        assertThat(violatedFields(withTimes(times))).containsExactly("timesPerPeriod");
    }

    @Test
    void timesPerPeriod_null_isRejected() {
        assertThat(violatedFields(withTimes(null))).containsExactly("timesPerPeriod");
    }

    @Test
    void amountPerSession_null_isAccepted() {
        assertThat(violatedFields(withAmount(null))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "99999999.99"})
    void amountPerSession_withinBounds_isAccepted(String amount) {
        assertThat(violatedFields(withAmount(new BigDecimal(amount)))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "100000000", "1.234"})
    void amountPerSession_outOfBounds_isRejected(String amount) {
        assertThat(violatedFields(withAmount(new BigDecimal(amount)))).containsExactly("amountPerSession");
    }

    @Test
    void unit_null_isAccepted() {
        assertThat(violatedFields(withUnit(null))).isEmpty();
    }

    @Test
    void unit_of50Chars_isAccepted() {
        assertThat(violatedFields(withUnit("x".repeat(50)))).isEmpty();
    }

    @Test
    void unit_of51Chars_isRejected() {
        assertThat(violatedFields(withUnit("x".repeat(51)))).containsExactly("unit");
    }

    @Test
    void update_reportsSameViolationsAsCreate_forInvalidInput() {
        String longDescription = "x".repeat(1001);
        String longUnit = "x".repeat(51);
        var create = new CreateGoalRequest("", longDescription, null, 0, new BigDecimal("-1"), longUnit);
        var update = new UpdateGoalRequest("", longDescription, null, 0, new BigDecimal("-1"), longUnit);

        assertThat(violatedFields(update)).containsExactlyInAnyOrder(
                "title", "description", "period", "timesPerPeriod", "amountPerSession", "unit");
        assertThat(violatedFields(update)).isEqualTo(violatedFields(create));
    }
}
