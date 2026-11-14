package no.kristiania.recommendationservice.integration.weather;

import no.kristiania.recommendationservice.integration.setup.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;

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
                .andExpect(jsonPath("$.weather.temperature").value(2.8))
                .andExpect(jsonPath("$.weather.condition").value("CLOUDY"))
                .andExpect(jsonPath("$.weather.day").value(false))
                .andExpect(jsonPath("$.rating").value("OK"));
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
                .andExpect(jsonPath("$.weather.latitude").value(59.9))
                .andExpect(jsonPath("$.weather.longitude").value(10.8));

        verifyOpenMeteoCalled(1);
    }
    @Test
    void get_afterTtl_fetchesAgain() throws Exception {
        openMeteoReturns(CLOUDY_NIGHT);
        getWeather(LAT, LON).andExpect(status().isOk());

        makeCachedWeatherOlder(LAT, LON, Duration.ofMinutes(30));
        getWeather(LAT, LON).andExpect(jsonPath("$.weather.stale").value(false));

        verifyOpenMeteoCalled(2);
    }

    @Test
    void get_openMeteoDownAfterTtl_returnsStaleWeather() throws Exception {
        openMeteoReturns(CLOUDY_NIGHT);
        getWeather(LAT, LON).andExpect(status().isOk());

        makeCachedWeatherOlder(LAT, LON, Duration.ofMinutes(30));
        openMeteoFails();

        getWeather(LAT, LON)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weather.stale").value(true))
                .andExpect(jsonPath("$.weather.temperature").value(2.8));
    }

    @Test
    void get_openMeteoReturnsNoCurrent_returns503() throws Exception {
        openMeteoReturns("{}");

        getWeather(LAT, LON)
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void get_latitudeOutOfRange_returns400() throws Exception {
        getWeather(200, LON)
                .andExpect(status().isBadRequest());

        verifyOpenMeteoCalled(0);
    }
    @Test
    void getMine_withOutdoorGoal_returnsWeather() throws Exception {
        insertOutdoorGoal(10L, 1L);
        openMeteoReturns(CLOUDY_NIGHT);

        getMyWeather(LAT, LON)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value("OK"));
    }

    @Test
    void getMine_withoutOutdoorGoal_returns204_andSkipsOpenMeteo() throws Exception {
        getMyWeather(LAT, LON)
                .andExpect(status().isNoContent());

        verifyOpenMeteoCalled(0);
    }

    @Test
    void getMine_onlyOtherUserHasOutdoorGoal_returns204() throws Exception {
        insertOutdoorGoal(11L, 2L);

        getMyWeather(LAT, LON)
                .andExpect(status().isNoContent());
    }
}
