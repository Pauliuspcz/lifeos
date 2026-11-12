package no.kristiania.recommendationservice.weather;

import no.kristiania.recommendationservice.weather.dto.WeatherRecommendation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
public class WeatherController {
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/weather")
    public WeatherRecommendation getWeather(@RequestParam double lat, @RequestParam double lon) {
        return weatherService.getRecommendation(lat, lon);
    }
}
