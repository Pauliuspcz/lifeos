package no.kristiania.recommendationservice.weather.dto;

import no.kristiania.recommendationservice.weather.enums.Rating;

public record WeatherRecommendation(WeatherResponse weather, Rating rating, String reason, String advice, BestTime bestTime) {
    public WeatherRecommendation(WeatherResponse weather, Rating rating, String reason, String advice) {
        this(weather, rating, reason, advice, null);
    }
}