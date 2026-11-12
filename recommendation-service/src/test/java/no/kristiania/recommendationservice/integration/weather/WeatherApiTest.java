package no.kristiania.recommendationservice.integration.weather;

import no.kristiania.recommendationservice.integration.setup.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WeatherApiTest extends AbstractIntegrationTest {

    private static final double LAT = 59.91;
    private static final double LON = 10.75;

    private static final String CLOUDY_NIGHT = """
            {"current": {"time": "2026-11-12T16:00", "temperature_2m": 2.8, "apparent_temperature": -0.3,
             "precipitation": 0.0, "weather_code": 3, "wind_speed_10m": 7.9, "is_day": 0}}""";

    @Test
    void get_returnsWeatherFromOpenMeteo() throws Exception {
        openMeteoReturns(CLOUDY_NIGHT);

        getWeather(LAT, LON)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(2.8))
                .andExpect(jsonPath("$.condition").value("CLOUDY"))
                .andExpect(jsonPath("$.day").value(false));
    }

    @Test
    void get_openMeteoDown_returns503() throws Exception {
        openMeteoFails();

        getWeather(LAT, LON)
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void get_openMeteoTooSlow_returns503() throws Exception {
        openMeteoRespondsAfter(1500, CLOUDY_NIGHT);

        getWeather(LAT, LON)
                .andExpect(status().isServiceUnavailable());
    }
    @Test
    void get_twiceWithinTtl_callsOpenMeteoOnce() throws Exception {
        openMeteoReturns(CLOUDY_NIGHT);

        getWeather(LAT, LON).andExpect(status().isOk());
        getWeather(LAT, LON).andExpect(status().isOk());

        verifyOpenMeteoCalled(1);
    }

    @Test
    void get_nearbyCoordinates_shareRoundedCacheEntry() throws Exception {
        openMeteoReturns(CLOUDY_NIGHT);

        getWeather(59.91, 10.75).andExpect(status().isOk());
        getWeather(59.88, 10.78)
                .andExpect(jsonPath("$.latitude").value(59.9))
                .andExpect(jsonPath("$.longitude").value(10.8));

        verifyOpenMeteoCalled(1);
    }
}
