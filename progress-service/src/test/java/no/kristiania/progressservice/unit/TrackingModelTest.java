package no.kristiania.progressservice.unit;

import no.kristiania.progressservice.client.GoalSummary;
import no.kristiania.progressservice.client.TrackingModel;
import no.kristiania.progressservice.progress.exceptions.UnsupportedTrackingException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrackingModelTest {

    private static GoalSummary goal(String trackingType, String period) {
        return new GoalSummary(1L, "ACTIVE", trackingType, 1, period, null, null, null, null);
    }

    @Test
    void checkDailyIsDailyCheck() {
        assertThat(TrackingModel.from(goal("CHECK", "DAILY"))).isEqualTo(TrackingModel.DAILY_CHECK);
    }

    @Test
    void checkWeeklyIsWeeklyCount() {
        assertThat(TrackingModel.from(goal("CHECK", "WEEKLY"))).isEqualTo(TrackingModel.WEEKLY_COUNT);
    }

    @Test
    void checkMonthlyIsMonthlyCount() {
        assertThat(TrackingModel.from(goal("CHECK", "MONTHLY"))).isEqualTo(TrackingModel.MONTHLY_COUNT);
    }

    @Test
    void amountIsCumulativeSum() {
        assertThat(TrackingModel.from(goal("AMOUNT", "MONTHLY"))).isEqualTo(TrackingModel.CUMULATIVE_SUM);
    }

    @Test
    void checkYearlyThrows() {
        assertThatThrownBy(() -> TrackingModel.from(goal("CHECK", "YEARLY")))
                .isInstanceOf(UnsupportedTrackingException.class);
    }
}
