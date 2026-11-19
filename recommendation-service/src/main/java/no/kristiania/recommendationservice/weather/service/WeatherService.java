package no.kristiania.recommendationservice.weather.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.recommendationservice.exceptions.WeatherUnavailableException;
import no.kristiania.recommendationservice.outdoor.repository.OutdoorGoalRepository;
import no.kristiania.recommendationservice.weather.dto.BestTime;
import no.kristiania.recommendationservice.weather.dto.WeatherRecommendation;
import no.kristiania.recommendationservice.weather.model.CachedWeather;
import no.kristiania.recommendationservice.weather.model.Location;
import no.kristiania.recommendationservice.weather.openmeteo.OpenMeteoClient;
import no.kristiania.recommendationservice.weather.openmeteo.OpenMeteoMapper;
import no.kristiania.recommendationservice.weather.openmeteo.OpenMeteoResponse;
import no.kristiania.recommendationservice.weather.props.WeatherCacheProperties;
import no.kristiania.recommendationservice.weather.props.WeatherRatingProperties;
import no.kristiania.recommendationservice.weather.rules.BestWindow;
import no.kristiania.recommendationservice.weather.rules.WeatherRating;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
public class WeatherService {

    private final OpenMeteoClient openMeteoClient;
    private final OutdoorGoalRepository outdoorGoalRepository;

    private final Cache<Location, CachedWeather> weatherCache;
    private final WeatherCacheProperties weatherCacheProperties;
    private final WeatherRatingProperties weatherRatingProperties;

    public WeatherService(OpenMeteoClient openMeteoClient, OutdoorGoalRepository outdoorGoalRepository,
                          Cache<Location, CachedWeather> weatherCache,
                          WeatherCacheProperties weatherCacheProperties,
                          WeatherRatingProperties weatherRatingProperties) {
        this.openMeteoClient = openMeteoClient;
        this.outdoorGoalRepository = outdoorGoalRepository;
        this.weatherCache = weatherCache;
        this.weatherCacheProperties = weatherCacheProperties;
        this.weatherRatingProperties = weatherRatingProperties;
    }

    public WeatherRecommendation getRecommendation(double lat, double lon) {
        CachedWeather data = getWeather(lat, lon);
        WeatherRecommendation rated = WeatherRating.rate(data.weather(), weatherRatingProperties);
        BestTime found = BestWindow.find(data.forecast(), data.weather().observedAt(), weatherRatingProperties);
        BestTime bestTime = found != null && found.rating().isBetterThan(rated.rating()) ? found : null;
        String advice = bestTime != null ? null : rated.advice();
        return new WeatherRecommendation(rated.weather(), rated.rating(), rated.reason(), advice, bestTime);
    }

    public Optional<WeatherRecommendation> getRecommendationForUser(Long userId, double lat, double lon) {
        if (!outdoorGoalRepository.existsByUserId(userId)) {
            return Optional.empty();
        }
        return Optional.of(getRecommendation(lat, lon));
    }

    private CachedWeather getWeather(double lat, double lon) {
        Location location = Location.rounded(lat, lon);
        CachedWeather cached = weatherCache.getIfPresent(location);

        if (cached != null && cached.fetchedAt().isAfter(Instant.now().minus(weatherCacheProperties.freshTtl()))) {
            return cached;
        }
        try {
            CachedWeather fresh = fetch(location);
            weatherCache.put(location, fresh);
            return fresh;
        } catch (WeatherUnavailableException e) {
            if (cached == null) {
                throw e;
            }
            log.warn("Serving stale weather fetchedAt={}", cached.fetchedAt());
            return new CachedWeather(cached.weather().asStale(), cached.forecast(), cached.fetchedAt());
        }
    }

    private CachedWeather fetch(Location location) {
        OpenMeteoResponse response;
        try {
            response = openMeteoClient.getCurrent(location.lat(), location.lon());
        } catch (RestClientException e) {
            log.warn("Open-Meteo call failed type={}", e.getClass().getSimpleName());
            log.debug("Open-Meteo call failed", e);
            throw new WeatherUnavailableException();
        }
        if (response == null || response.current() == null) {
            log.warn("Open-Meteo returned no current weather");
            throw new WeatherUnavailableException();
        }
        log.debug("Weather fetched from Open-Meteo");
        return new CachedWeather(OpenMeteoMapper.current(response, location),
                OpenMeteoMapper.forecast(response.hourly(), location), Instant.now());
    }
}