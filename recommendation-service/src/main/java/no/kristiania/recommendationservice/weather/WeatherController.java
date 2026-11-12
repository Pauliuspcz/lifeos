package no.kristiania.recommendationservice.weather;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
    public WeatherRecommendation getWeather(@RequestParam @DecimalMin("-90") @DecimalMax("90") double lat,
                                            @RequestParam @DecimalMin("-180") @DecimalMax("180") double lon) {
        return weatherService.getRecommendation(lat, lon);
    }
}
