package no.kristiania.recommendationservice.weather.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class WeatherUnavailableException extends RuntimeException {
    public WeatherUnavailableException() { super("Weather data is unavailable"); }
}
