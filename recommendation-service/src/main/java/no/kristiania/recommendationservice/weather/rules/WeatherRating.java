package no.kristiania.recommendationservice.weather.rules;

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
        boolean wetGround = w.recentPrecipitationMm() > 0 && w.temperature() <= p.iceRiskMaxTemp();

        if (w.condition() == WeatherCondition.THUNDERSTORM) {
            return poor(w, "Thunderstorm", p);
        }
        if (w.condition() == WeatherCondition.FREEZING_RAIN || (w.precipitationMm() > 0 && nearZero) || wetGround) {
            return poor(w, "Risk of ice", p);
        }
        if (w.precipitationMm() >= p.heavyPrecipitationMm()) {
            return poor(w, "Heavy " + precipitationType + " (" + w.precipitationMm() + " mm)", p);
        }
        if (w.windSpeedKmh() >= p.poorWindKmh()) {
            return poor(w, "Strong wind (" + w.windSpeedKmh() + " km/h)", p);
        }
        if (w.feelsLike() <= p.poorColdFeelsLike() || w.feelsLike() >= p.poorHotFeelsLike()) {
            return poor(w, "Feels like " + w.feelsLike() + "°C", p);
        }
        if (w.temperature() < 0) {
            return ok(w, "Below freezing", "Wear shoes with good grip");
        }
        if (w.precipitationMm() > 0) {
            return ok(w, "Light " + precipitationType + " (" + w.precipitationMm() + " mm)", "Bring a rain jacket");
        }
        if (w.windSpeedKmh() >= p.okWindKmh()) {
            return ok(w, "Windy (" + w.windSpeedKmh() + " km/h)", "Pick a sheltered route");
        }
        if (w.feelsLike() < p.okColdFeelsLike()) {
            return ok(w, "Feels like " + w.feelsLike() + "°C", "Wear layers");
        }
        if (w.feelsLike() > p.okHotFeelsLike()) {
            return ok(w, "Feels like " + w.feelsLike() + "°C", "Bring water and avoid the midday heat");
        }
        return new WeatherRecommendation(w, Rating.GOOD, "Good weather for outdoor activity", null);
    }

    private static WeatherRecommendation poor(WeatherResponse w, String reason, WeatherRatingProperties p) {
        return new WeatherRecommendation(w, Rating.POOR, reason, p.poorAdvice());
    }

    private static WeatherRecommendation ok(WeatherResponse w, String reason, String advice) {
        return new WeatherRecommendation(w, Rating.OK, reason, advice);
    }
}