package no.kristiania.recommendationservice.unit.weather;

import no.kristiania.recommendationservice.weather.BestWindow;
import no.kristiania.recommendationservice.weather.dto.BestTime;
import no.kristiania.recommendationservice.weather.dto.WeatherResponse;
import no.kristiania.recommendationservice.weather.enums.Rating;
import no.kristiania.recommendationservice.weather.enums.WeatherCondition;
import no.kristiania.recommendationservice.weather.props.WeatherRatingProperties;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BestWindowTest {
    private static final WeatherRatingProperties PROPS =
            new WeatherRatingProperties(1.0, 40, 25, -10, 32, 0, 27, -2, 2, "Go indoors");
    private static final LocalDateTime MORNING = LocalDateTime.of(2026, 11, 12, 10, 15);

    private static WeatherResponse hour(LocalDateTime time, double precipitation) {
        WeatherCondition condition = precipitation > 0 ? WeatherCondition.RAIN : WeatherCondition.CLEAR;
        return new WeatherResponse(59.9, 10.8, time, 10, 10, precipitation, 0, 5, condition, true, false);
    }

    private static WeatherResponse at(int hourOfDay, double precipitation) {
        return hour(LocalDateTime.of(2026, 11, 12, hourOfDay, 0), precipitation);
    }

    @Test
    void findsFirstGoodStretch() {
        List<WeatherResponse> forecast = List.of(at(10, 2.0), at(11, 2.0), at(12, 0), at(13, 0), at(14, 2.0));
        BestTime best = BestWindow.find(forecast, MORNING, PROPS);
        assertThat(best.from()).isEqualTo(LocalDateTime.of(2026, 11, 12, 12, 0));
        assertThat(best.to()).isEqualTo(LocalDateTime.of(2026, 11, 12, 14, 0));
        assertThat(best.rating()).isEqualTo(Rating.GOOD);
    }

    @Test
    void fallsBackToOkWhenNoGoodHour() {
        List<WeatherResponse> forecast = List.of(at(10, 2.0), at(11, 0.3), at(12, 0.3), at(13, 2.0));
        BestTime best = BestWindow.find(forecast, MORNING, PROPS);
        assertThat(best.from()).isEqualTo(LocalDateTime.of(2026, 11, 12, 11, 0));
        assertThat(best.rating()).isEqualTo(Rating.OK);
    }

    @Test
    void allPoorGivesNull() {
        List<WeatherResponse> forecast = List.of(at(10, 2.0), at(11, 2.0));
        assertThat(BestWindow.find(forecast, MORNING, PROPS)).isNull();
    }

    @Test
    void ignoresPastHours() {
        List<WeatherResponse> forecast = List.of(at(8, 0), at(9, 0), at(10, 2.0), at(11, 0));
        BestTime best = BestWindow.find(forecast, MORNING, PROPS);
        assertThat(best.from()).isEqualTo(LocalDateTime.of(2026, 11, 12, 11, 0));
    }

    @Test
    void ignoresNightAndTomorrow() {
        LocalDateTime evening = LocalDateTime.of(2026, 11, 12, 20, 15);
        List<WeatherResponse> forecast = List.of(at(20, 2.0), at(21, 2.0), at(22, 0), at(23, 0),
                hour(LocalDateTime.of(2026, 11, 13, 7, 0), 0));
        assertThat(BestWindow.find(forecast, evening, PROPS)).isNull();
    }
}
