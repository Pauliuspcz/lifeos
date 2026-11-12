package no.kristiania.recommendationservice.weather.dto;

import no.kristiania.recommendationservice.weather.enums.WeatherCondition;
import java.time.LocalDateTime;

public record WeatherResponse(
        double latitude,
        double longitude,
        LocalDateTime observedAt,
        double temperature,
        double feelsLike,
        double precipitationMm,
        double windSpeedKmh,
        WeatherCondition condition,
        boolean day
) {}
