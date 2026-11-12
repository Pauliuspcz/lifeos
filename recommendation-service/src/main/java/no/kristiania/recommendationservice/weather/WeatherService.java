package no.kristiania.recommendationservice.weather;

import lombok.extern.slf4j.Slf4j;
import no.kristiania.recommendationservice.weather.dto.OpenMeteoResponse;
import no.kristiania.recommendationservice.weather.dto.WeatherResponse;
import no.kristiania.recommendationservice.weather.enums.WeatherCondition;
import no.kristiania.recommendationservice.weather.exceptions.WeatherUnavailableException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import com.github.benmanes.caffeine.cache.Cache;

@Slf4j
@Service
public class WeatherService {
    private final OpenMeteoClient openMeteoClient;

    private final Cache<Location, WeatherResponse> weatherCache;

    public WeatherService(OpenMeteoClient openMeteoClient, Cache<Location, WeatherResponse> weatherCache) {
        this.openMeteoClient = openMeteoClient;
        this.weatherCache = weatherCache;
    }

    public WeatherResponse getWeather(double lat, double lon) {
        return weatherCache.get(Location.rounded(lat, lon), this::fetch);
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
                current.isDay() == 1
        );
    }
}