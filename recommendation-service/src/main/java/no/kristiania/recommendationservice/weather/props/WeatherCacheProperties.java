package no.kristiania.recommendationservice.weather.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.weather.cache")
public record WeatherCacheProperties(Duration freshTtl, Duration staleMax, long maxEntries) {}
