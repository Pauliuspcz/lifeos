package no.kristiania.recommendationservice.weather;

import no.kristiania.recommendationservice.weather.dto.WeatherResponse;
import java.time.Instant;

public record CachedWeather(WeatherResponse weather, Instant fetchedAt) {}
