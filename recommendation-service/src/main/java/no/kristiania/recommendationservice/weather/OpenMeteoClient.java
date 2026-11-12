package no.kristiania.recommendationservice.weather;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenMeteoClient {
    private final RestClient openMeteoRestClient;

    public OpenMeteoClient(RestClient openMeteoRestClient) {
        this.openMeteoRestClient = openMeteoRestClient;
    }

    public String getCurrent(double lat, double lon) {
        return openMeteoRestClient.get()
                .uri(uri -> uri.path("/v1/forecast")
                        .queryParam("latitude", lat)
                        .queryParam("longitude", lon)
                        .queryParam("current", "temperature_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,is_day")
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .body(String.class);
    }
}
