package no.kristiania.recommendationservice.weather.openmeteo;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenMeteoClient {
    private final RestClient openMeteoRestClient;
    public static final int PAST_HOURS = 12;
    private static final String FIELDS =
            "temperature_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,is_day";

    public OpenMeteoClient(RestClient openMeteoRestClient) {
        this.openMeteoRestClient = openMeteoRestClient;
    }

    public OpenMeteoResponse getCurrent(double lat, double lon) {
        return openMeteoRestClient.get()
                .uri(uri -> uri.path("/v1/forecast")
                        .queryParam("latitude", lat)
                        .queryParam("longitude", lon)
                        .queryParam("current", FIELDS)
                        .queryParam("hourly", FIELDS)
                        .queryParam("past_hours", PAST_HOURS)
                        .queryParam("forecast_hours", 12)
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .body(OpenMeteoResponse.class);
    }
}
