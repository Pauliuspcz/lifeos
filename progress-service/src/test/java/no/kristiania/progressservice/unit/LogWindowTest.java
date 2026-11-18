package no.kristiania.progressservice.unit;

import no.kristiania.progressservice.client.TrackingModel;
import no.kristiania.progressservice.progress.LogWindow;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LogWindowTest {
    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 16);
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 11, 18);
    private static final LocalDate SUNDAY = LocalDate.of(2026, 11, 22);

    @Test
    void dailyCanLogTwoDaysBack() {
        assertThat(LogWindow.earliest(TrackingModel.DAILY_CHECK, WEDNESDAY)).isEqualTo(MONDAY);
    }

    @Test
    void amountCanLogTwoDaysBack() {
        assertThat(LogWindow.earliest(TrackingModel.CUMULATIVE_SUM, WEDNESDAY)).isEqualTo(MONDAY);
    }

    @Test
    void weeklyCanLogBackToMonday() {
        assertThat(LogWindow.contains(TrackingModel.WEEKLY_COUNT, SUNDAY, MONDAY)).isTrue();
    }

    @Test
    void weeklyCannotLogLastWeekOnMonday() {
        assertThat(LogWindow.contains(TrackingModel.WEEKLY_COUNT, SUNDAY.plusDays(1), SUNDAY)).isFalse();
    }

    @Test
    void futureDateIsOutsideWindow() {
        assertThat(LogWindow.contains(TrackingModel.DAILY_CHECK, WEDNESDAY, WEDNESDAY.plusDays(1))).isFalse();
    }

    @Test
    void monthlyCanLogAtMostSevenDaysBack() {
        LocalDate friday = LocalDate.of(2026, 11, 20);
        assertThat(LogWindow.earliest(TrackingModel.MONTHLY_COUNT, friday))
                .isEqualTo(LocalDate.of(2026, 11, 14));
    }

    @Test
    void monthlyCannotLogLastMonth() {
        LocalDate dec3 = LocalDate.of(2026, 12, 3);
        assertThat(LogWindow.earliest(TrackingModel.MONTHLY_COUNT, dec3))
                .isEqualTo(LocalDate.of(2026, 12, 1));
    }
}
