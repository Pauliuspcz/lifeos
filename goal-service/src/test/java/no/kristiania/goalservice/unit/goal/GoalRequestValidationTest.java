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

    private static CreateGoalRequest create(String title, String description, GoalPeriod period,
                                            Integer times, BigDecimal amount, String unit,
                                            String motivation, String iconKey, String color) {
        return new CreateGoalRequest(title, description, null, null, period, times, amount, unit,
                null, null, null, null, null, motivation, iconKey, color);
    }

    private static UpdateGoalRequest update(String title, String description, GoalPeriod period,
                                            Integer times, BigDecimal amount, String unit,
                                            String motivation, String iconKey, String color) {
        return new UpdateGoalRequest(title, description, period, times, amount, unit,
                null, null, null, motivation, iconKey, color, null);
    }

    private static CreateGoalRequest valid() {
        return create(TITLE, DESCRIPTION, PERIOD, TIMES, AMOUNT, UNIT, null, null, null);
    }

    private static CreateGoalRequest withTitle(String v) { return create(v, DESCRIPTION, PERIOD, TIMES, AMOUNT, UNIT, null, null, null); }
    private static CreateGoalRequest withDescription(String v) { return create(TITLE, v, PERIOD, TIMES, AMOUNT, UNIT, null, null, null); }
    private static CreateGoalRequest withPeriod(GoalPeriod v) { return create(TITLE, DESCRIPTION, v, TIMES, AMOUNT, UNIT, null, null, null); }
    private static CreateGoalRequest withTimes(Integer v) { return create(TITLE, DESCRIPTION, PERIOD, v, AMOUNT, UNIT, null, null, null); }
    private static CreateGoalRequest withAmount(BigDecimal v) { return create(TITLE, DESCRIPTION, PERIOD, TIMES, v, UNIT, null, null, null); }
    private static CreateGoalRequest withUnit(String v) { return create(TITLE, DESCRIPTION, PERIOD, TIMES, AMOUNT, v, null, null, null); }
    private static CreateGoalRequest withMotivation(String v) { return create(TITLE, DESCRIPTION, PERIOD, TIMES, AMOUNT, UNIT, v, null, null); }
    private static CreateGoalRequest withIconKey(String v) { return create(TITLE, DESCRIPTION, PERIOD, TIMES, AMOUNT, UNIT, null, v, null); }
    private static CreateGoalRequest withColor(String v) { return create(TITLE, DESCRIPTION, PERIOD, TIMES, AMOUNT, UNIT, null, null, v); }

    private static Set<String> violatedFields(Object request) {
        return VALIDATOR.validate(request).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(violatedFields(valid())).isEmpty();
    }

    @Test
    void minimalRequest_onlyTitle_hasNoViolations() {
        var request = create(TITLE, null, null, null, null, null, null, null, null);

        assertThat(violatedFields(request)).isEmpty();
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
    void period_null_isAccepted() {
        // Requirement per trackingType is enforced in GoalService, not by bean validation
        assertThat(violatedFields(withPeriod(null))).isEmpty();
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
    void timesPerPeriod_null_isAccepted() {
        assertThat(violatedFields(withTimes(null))).isEmpty();
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
    void motivation_of500Chars_isAccepted() {
        assertThat(violatedFields(withMotivation("x".repeat(500)))).isEmpty();
    }

    @Test
    void motivation_of501Chars_isRejected() {
        assertThat(violatedFields(withMotivation("x".repeat(501)))).containsExactly("motivation");
    }

    @Test
    void iconKey_of50Chars_isAccepted() {
        assertThat(violatedFields(withIconKey("x".repeat(50)))).isEmpty();
    }

    @Test
    void iconKey_of51Chars_isRejected() {
        assertThat(violatedFields(withIconKey("x".repeat(51)))).containsExactly("iconKey");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"#FF5722", "#aabbcc", "#000000"})
    void color_nullOrHex_isAccepted(String color) {
        assertThat(violatedFields(withColor(color))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "FF5722", "#FFF", "#GGGGGG", "red", "#FF57222"})
    void color_notSixDigitHex_isRejected(String color) {
        assertThat(violatedFields(withColor(color))).containsExactly("color");
    }

    @Test
    void update_reportsSameViolationsAsCreate_forInvalidInput() {
        String longDescription = "x".repeat(1001);
        String longUnit = "x".repeat(51);
        String longMotivation = "x".repeat(501);
        String longIcon = "x".repeat(51);
        var create = create("", longDescription, null, 0, new BigDecimal("-1"), longUnit,
                longMotivation, longIcon, "red");
        var update = update("", longDescription, null, 0, new BigDecimal("-1"), longUnit,
                longMotivation, longIcon, "red");

        assertThat(violatedFields(update)).containsExactlyInAnyOrder(
                "title", "description", "timesPerPeriod", "amountPerSession", "unit",
                "motivation", "iconKey", "color");
        assertThat(violatedFields(update)).isEqualTo(violatedFields(create));
    }
}