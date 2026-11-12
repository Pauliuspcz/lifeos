package no.kristiania.recommendationservice.weather;

import lombok.extern.slf4j.Slf4j;
import no.kristiania.recommendationservice.weather.dto.OpenMeteoResponse;
import no.kristiania.recommendationservice.weather.dto.WeatherRecommendation;
import no.kristiania.recommendationservice.weather.dto.WeatherResponse;
import no.kristiania.recommendationservice.weather.enums.WeatherCondition;
import no.kristiania.recommendationservice.weather.exceptions.WeatherUnavailableException;
import no.kristiania.recommendationservice.weather.props.WeatherCacheProperties;
import no.kristiania.recommendationservice.weather.props.WeatherRatingProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.Instant;

@Slf4j
@Service
public class WeatherService {
    private final OpenMeteoClient openMeteoClient;

    private final Cache<Location, CachedWeather> weatherCache;
    private final WeatherCacheProperties weatherCacheProperties;
    private final WeatherRatingProperties weatherRatingProperties;

    public WeatherService(OpenMeteoClient openMeteoClient, Cache<Location, CachedWeather> weatherCache,
                          WeatherCacheProperties weatherCacheProperties, WeatherRatingProperties weatherRatingProperties) {
        this.openMeteoClient = openMeteoClient;
        this.weatherCache = weatherCache;
        this.weatherCacheProperties = weatherCacheProperties;
        this.weatherRatingProperties = weatherRatingProperties;
    }

    public WeatherRecommendation getRecommendation(double lat, double lon) {
        return WeatherRating.rate(getWeather(lat, lon), weatherRatingProperties);
    }

    public WeatherResponse getWeather(double lat, double lon) {
        Location location = Location.rounded(lat, lon);
        CachedWeather cached = weatherCache.getIfPresent(location);

        if (cached != null && cached.fetchedAt().isAfter(Instant.now().minus(weatherCacheProperties.freshTtl())
        )) {
            return cached.weather();
        }
        try {
            WeatherResponse fresh = fetch(location);
            weatherCache.put(location, new CachedWeather(fresh, Instant.now()));
            return fresh;
        } catch (WeatherUnavailableException e) {
            if (cached == null) {
                throw e;
            }
            log.warn("Serving stale weather for {} fetched at {}", location, cached.fetchedAt());
            return cached.weather().asStale();
        }
    }

    private WeatherResponse fetch(Location location) {
        log.info("Fetching weather from Open-Meteo for {}", location);
        OpenMeteoResponse.Current current;
        try {
            current = openMeteoClient.getCurrent(location.lat(), location.lon()).current();
        } catch (RestClientException e) {
            log.warn("Open-Meteo call failed for {}: {}", location, e.getMessage());
            throw new WeatherUnavailableException();
        }
        return new WeatherResponse(
                location.lat(), location.lon(),
                current.time(),
                current.temperature(),
                current.apparentTemperature(),
                current.precipitation(),
                current.windSpeed(),
                WeatherCondition.fromWmoCode(current.weatherCode()),
                current.isDay() == 1,
                false
        );
    }
}