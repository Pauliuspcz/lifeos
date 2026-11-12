package no.kristiania.recommendationservice.weather.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.weather.rating")
public record WeatherRatingProperties(
        double heavyPrecipitationMm,
        double poorWindKmh,
        double okWindKmh,
        double poorColdFeelsLike,
        double poorHotFeelsLike,
        double okColdFeelsLike,
        double okHotFeelsLike,
        double iceRiskMinTemp,
        double iceRiskMaxTemp
) {}
