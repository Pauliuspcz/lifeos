package no.kristiania.recommendationservice.weather.dto;

import no.kristiania.recommendationservice.weather.enums.Rating;

public record WeatherRecommendation(WeatherResponse weather, Rating rating, String reason) {}