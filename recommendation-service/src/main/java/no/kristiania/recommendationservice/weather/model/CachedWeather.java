package no.kristiania.recommendationservice.weather.model;

import no.kristiania.recommendationservice.weather.dto.WeatherResponse;
import java.time.Instant;
import java.util.List;

public record CachedWeather(WeatherResponse weather, List<WeatherResponse> forecast, Instant fetchedAt) {}
