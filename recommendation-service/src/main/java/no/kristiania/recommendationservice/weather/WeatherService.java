package no.kristiania.recommendationservice.weather;

import lombok.extern.slf4j.Slf4j;
import no.kristiania.recommendationservice.weather.dto.OpenMeteoResponse;
import no.kristiania.recommendationservice.weather.dto.WeatherResponse;
import no.kristiania.recommendationservice.weather.enums.WeatherCondition;
import no.kristiania.recommendationservice.weather.exceptions.WeatherUnavailableException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class WeatherService {
    private final OpenMeteoClient openMeteoClient;

    public WeatherService(OpenMeteoClient openMeteoClient) {
        this.openMeteoClient = openMeteoClient;
    }

    public WeatherResponse getWeather(double lat, double lon) {
        OpenMeteoResponse.Current current;
        try {
            current = openMeteoClient.getCurrent(lat, lon).current();
        } catch (RestClientException e) {
            log.warn("Open-Meteo call failed for {},{}: {}", lat, lon, e.getMessage());
            throw new WeatherUnavailableException();
        }
        return new WeatherResponse(
                lat, lon,
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