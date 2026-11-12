package no.kristiania.recommendationservice.weather;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
public class WeatherController {
    private final OpenMeteoClient openMeteoClient;

    public WeatherController(OpenMeteoClient openMeteoClient) {
        this.openMeteoClient = openMeteoClient;
    }

    @GetMapping("/weather")
    public String getWeather(@RequestParam double lat, @RequestParam double lon) {
        return openMeteoClient.getCurrent(lat, lon);
    }
}
