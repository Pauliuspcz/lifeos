package no.kristiania.recommendationservice.weather.openmeteo;

import no.kristiania.recommendationservice.weather.Location;
import no.kristiania.recommendationservice.weather.dto.WeatherResponse;
import no.kristiania.recommendationservice.weather.enums.WeatherCondition;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public final class OpenMeteoMapper {
    private OpenMeteoMapper() {}

    public static WeatherResponse current(OpenMeteoResponse response, Location location) {
        OpenMeteoResponse.Current c = response.current();
        return new WeatherResponse(location.lat(), location.lon(), c.time(),
                c.temperature(), c.apparentTemperature(), c.precipitation(),
                recentPrecipitation(response.hourly(), c.time()), c.windSpeed(),
                WeatherCondition.fromWmoCode(c.weatherCode()), c.isDay() == 1, false);
    }

    public static List<WeatherResponse> forecast(OpenMeteoResponse.Hourly h, Location location) {
        List<WeatherResponse> hours = new ArrayList<>();
        if (h == null || h.time() == null || h.temperature() == null) {
            return hours;
        }
        for (int i = 0; i < h.time().size(); i++) {
            if (h.temperature().get(i) == null) continue;
            LocalDateTime time = h.time().get(i);
            hours.add(new WeatherResponse(location.lat(), location.lon(), time,
                    h.temperature().get(i), h.apparentTemperature().get(i), h.precipitation().get(i),
                    recentPrecipitation(h, time), h.windSpeed().get(i),
                    WeatherCondition.fromWmoCode(h.weatherCode().get(i)), h.isDay().get(i) == 1, false));
        }
        return hours;
    }

    private static double recentPrecipitation(OpenMeteoResponse.Hourly h, LocalDateTime now) {
        if (h == null || h.time() == null || h.precipitation() == null) {
            return 0;
        }
        LocalDateTime from = now.minusHours(OpenMeteoClient.PAST_HOURS);
        double sum = 0;
        for (int i = 0; i < h.time().size(); i++) {
            LocalDateTime time = h.time().get(i);
            Double mm = h.precipitation().get(i);
            if (mm != null && time.isAfter(from) && !time.isAfter(now)) {
                sum += mm;
            }
        }
        return Math.round(sum * 10) / 10.0;
    }
}
