package no.kristiania.recommendationservice.weather;

import no.kristiania.recommendationservice.weather.dto.WeatherRecommendation;
import no.kristiania.recommendationservice.weather.dto.WeatherResponse;
import no.kristiania.recommendationservice.weather.enums.Rating;
import no.kristiania.recommendationservice.weather.enums.WeatherCondition;
import no.kristiania.recommendationservice.weather.props.WeatherRatingProperties;

public final class WeatherRating {

    private WeatherRating() {}

    public static WeatherRecommendation rate(WeatherResponse w, WeatherRatingProperties p) {
        String precipitationType = w.condition() == WeatherCondition.SNOW ? "snow" : "rain";
        boolean nearZero = w.temperature() >= p.iceRiskMinTemp() && w.temperature() <= p.iceRiskMaxTemp();

        if (w.condition() == WeatherCondition.THUNDERSTORM) {
            return poor(w, "Thunderstorm");
        }
        if (w.condition() == WeatherCondition.FREEZING_RAIN || (w.precipitationMm() > 0 && nearZero)) {
            return poor(w, "Risk of ice");
        }
        if (w.precipitationMm() >= p.heavyPrecipitationMm()) {
            return poor(w, "Heavy " + precipitationType + " (" + w.precipitationMm() + " mm)");
        }
        if (w.windSpeedKmh() >= p.poorWindKmh()) {
            return poor(w, "Strong wind (" + w.windSpeedKmh() + " km/h)");
        }
        if (w.feelsLike() <= p.poorColdFeelsLike() || w.feelsLike() >= p.poorHotFeelsLike()) {
            return poor(w, "Feels like " + w.feelsLike() + "°C");
        }

        if (w.temperature() < 0) {
            return ok(w, "Below freezing – watch for icy paths");
        }
        if (w.precipitationMm() > 0) {
            return ok(w, "Light " + precipitationType + " (" + w.precipitationMm() + " mm)");
        }
        if (w.windSpeedKmh() >= p.okWindKmh()) {
            return ok(w, "Windy (" + w.windSpeedKmh() + " km/h)");
        }
        if (w.feelsLike() < p.okColdFeelsLike() || w.feelsLike() > p.okHotFeelsLike()) {
            return ok(w, "Feels like " + w.feelsLike() + "°C");
        }
        return new WeatherRecommendation(w, Rating.GOOD, "Good weather for outdoor activity");
    }

    private static WeatherRecommendation poor(WeatherResponse w, String reason) {
        return new WeatherRecommendation(w, Rating.POOR, reason);
    }

    private static WeatherRecommendation ok(WeatherResponse w, String reason) {
        return new WeatherRecommendation(w, Rating.OK, reason);
    }
}