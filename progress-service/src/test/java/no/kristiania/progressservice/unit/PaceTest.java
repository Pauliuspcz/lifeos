package no.kristiania.progressservice.unit;

import no.kristiania.progressservice.progress.utils.Pace;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PaceTest {
    private static final BigDecimal TARGET = new BigDecimal("50");
    private static final LocalDate START = LocalDate.of(2026, 11, 18);
    private static final LocalDate END = LocalDate.of(2026, 12, 17);

    @Test
    void firstDayExpectsOneDayShare() {
        assertThat(Pace.expected(TARGET, START, END, START)).isEqualByComparingTo("1.67");
    }

    @Test
    void lastDayExpectsFullTarget() {
        assertThat(Pace.expected(TARGET, START, END, END)).isEqualByComparingTo("50");
    }

    @Test
    void afterEndStaysAtTarget() {
        assertThat(Pace.expected(TARGET, START, END, END.plusDays(5))).isEqualByComparingTo("50");
    }

    @Test
    void missingEndDateGivesNull() {
        assertThat(Pace.expected(TARGET, START, null, START)).isNull();
    }
}

