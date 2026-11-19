package no.kristiania.recommendationservice.exceptions;

import org.springframework.http.HttpStatus;

public class WeatherUnavailableException extends ApiException {
    public WeatherUnavailableException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, "Weather data is unavailable");
    }
}
