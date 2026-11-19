package no.kristiania.reportservice.unit.report;

import no.kristiania.reportservice.daylog.utils.WellbeingStats;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WellbeingStatsTest {

    @Test
    void averageSkipsEmptyValues() {
        assertThat(WellbeingStats.average(Arrays.asList(4, null, 3))).isEqualTo(3.5);
    }

    @Test
    void averageOfNothingIsNull() {
        assertThat(WellbeingStats.average(List.of())).isNull();
    }
}
