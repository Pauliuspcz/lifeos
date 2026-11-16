package no.kristiania.recommendationservice.weather.openmeteo;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public record OpenMeteoResponse(Current current, Hourly hourly) {

    public record Current(
            LocalDateTime time,
            @JsonProperty("temperature_2m") double temperature,
            @JsonProperty("apparent_temperature") double apparentTemperature,
            double precipitation,
            @JsonProperty("weather_code") int weatherCode,
            @JsonProperty("wind_speed_10m") double windSpeed,
            @JsonProperty("is_day") int isDay
    ) {}

    public record Hourly(
            List<LocalDateTime> time,
            @JsonProperty("temperature_2m") List<Double> temperature,
            @JsonProperty("apparent_temperature") List<Double> apparentTemperature,
            List<Double> precipitation,
            @JsonProperty("weather_code") List<Integer> weatherCode,
            @JsonProperty("wind_speed_10m") List<Double> windSpeed,
            @JsonProperty("is_day") List<Integer> isDay
    ) {}
}