package no.kristiania.recommendationservice.unit;

import no.kristiania.recommendationservice.weather.WeatherRating;
import no.kristiania.recommendationservice.weather.dto.WeatherRecommendation;
import no.kristiania.recommendationservice.weather.dto.WeatherResponse;
import no.kristiania.recommendationservice.weather.enums.Rating;
import no.kristiania.recommendationservice.weather.enums.WeatherCondition;
import no.kristiania.recommendationservice.weather.props.WeatherRatingProperties;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherRatingTest {
    private static final WeatherRatingProperties PROPS =
            new WeatherRatingProperties(1.0, 40, 25, -10, 32, 0, 27, -2, 2);

    private static WeatherRecommendation rate(WeatherCondition condition, double temp, double feelsLike,
                                              double precipitation, double wind) {
        WeatherResponse weather = new WeatherResponse(59.9, 10.8, LocalDateTime.of(2026, 11, 12, 12, 0),
                temp, feelsLike, precipitation, wind, condition, true, false);
        return WeatherRating.rate(weather, PROPS);
    }

    @Test
    void thunderstormIsPoor() {
        WeatherRecommendation result = rate(WeatherCondition.THUNDERSTORM, 15, 15, 0.5, 10);
        assertThat(result.rating()).isEqualTo(Rating.POOR);
        assertThat(result.reason()).isEqualTo("Thunderstorm");
    }

    @Test
    void freezingRainIsPoor() {
        WeatherRecommendation result = rate(WeatherCondition.FREEZING_RAIN, -4, -8, 0.2, 5);
        assertThat(result.rating()).isEqualTo(Rating.POOR);
        assertThat(result.reason()).isEqualTo("Risk of ice");
    }

    @Test
    void rainAroundZeroIsRiskOfIce() {
        WeatherRecommendation result = rate(WeatherCondition.RAIN, 1, -2, 0.3, 5);
        assertThat(result.rating()).isEqualTo(Rating.POOR);
        assertThat(result.reason()).isEqualTo("Risk of ice");
    }

    @Test
    void heavyRainIsPoor() {
        WeatherRecommendation result = rate(WeatherCondition.RAIN, 8, 6, 1.5, 5);
        assertThat(result.rating()).isEqualTo(Rating.POOR);
        assertThat(result.reason()).isEqualTo("Heavy rain (1.5 mm)");
    }

    @Test
    void heavySnowSaysSnow() {
        WeatherRecommendation result = rate(WeatherCondition.SNOW, -5, -8, 1.2, 5);
        assertThat(result.rating()).isEqualTo(Rating.POOR);
        assertThat(result.reason()).isEqualTo("Heavy snow (1.2 mm)");
    }

    @Test
    void strongWindIsPoor() {
        WeatherRecommendation result = rate(WeatherCondition.CLOUDY, 10, 5, 0, 45);
        assertThat(result.rating()).isEqualTo(Rating.POOR);
        assertThat(result.reason()).isEqualTo("Strong wind (45.0 km/h)");
    }

    @Test
    void veryColdFeelsLikeIsPoor() {
        WeatherRecommendation result = rate(WeatherCondition.CLEAR, -12, -15, 0, 5);
        assertThat(result.rating()).isEqualTo(Rating.POOR);
        assertThat(result.reason()).isEqualTo("Feels like -15.0°C");
    }

    @Test
    void belowFreezingIsOkWithIceWarning() {
        WeatherRecommendation result = rate(WeatherCondition.CLEAR, -3, -5, 0, 5);
        assertThat(result.rating()).isEqualTo(Rating.OK);
        assertThat(result.reason()).isEqualTo("Below freezing – watch for icy paths");
    }

    @Test
    void lightRainIsOk() {
        WeatherRecommendation result = rate(WeatherCondition.RAIN, 6, 4, 0.4, 5);
        assertThat(result.rating()).isEqualTo(Rating.OK);
        assertThat(result.reason()).isEqualTo("Light rain (0.4 mm)");
    }

    @Test
    void windyIsOk() {
        WeatherRecommendation result = rate(WeatherCondition.CLOUDY, 10, 8, 0, 30);
        assertThat(result.rating()).isEqualTo(Rating.OK);
        assertThat(result.reason()).isEqualTo("Windy (30.0 km/h)");
    }

    @Test
    void chillyFeelsLikeIsOk() {
        WeatherRecommendation result = rate(WeatherCondition.CLOUDY, 3, -1, 0, 5);
        assertThat(result.rating()).isEqualTo(Rating.OK);
        assertThat(result.reason()).isEqualTo("Feels like -1.0°C");
    }

    @Test
    void mildAndCalmIsGood() {
        WeatherRecommendation result = rate(WeatherCondition.CLEAR, 15, 15, 0, 5);
        assertThat(result.rating()).isEqualTo(Rating.GOOD);
    }
}
