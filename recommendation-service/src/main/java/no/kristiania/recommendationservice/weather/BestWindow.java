package no.kristiania.recommendationservice.weather;

import no.kristiania.recommendationservice.weather.dto.BestTime;
import no.kristiania.recommendationservice.weather.dto.WeatherResponse;
import no.kristiania.recommendationservice.weather.enums.Rating;
import no.kristiania.recommendationservice.weather.props.WeatherRatingProperties;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public final class BestWindow {
    private static final int EARLIEST_HOUR = 6;
    private static final int LATEST_HOUR = 22;
    private BestWindow() {}

    public static BestTime find(List<WeatherResponse> forecast, LocalDateTime now, WeatherRatingProperties p) {
        List<WeatherResponse> hours = forecast.stream()
                .filter(h -> isLaterToday(h.observedAt(), now))
                .toList();
        BestTime good = firstStretch(hours, Rating.GOOD, p);
        return good != null ? good : firstStretch(hours, Rating.OK, p);
    }

    private static boolean isLaterToday(LocalDateTime time, LocalDateTime now) {
        return !time.isBefore(now.truncatedTo(ChronoUnit.HOURS))
                && time.toLocalDate().equals(now.toLocalDate())
                && time.getHour() >= EARLIEST_HOUR && time.getHour() < LATEST_HOUR;
    }

    private static BestTime firstStretch(List<WeatherResponse> hours, Rating wanted, WeatherRatingProperties p) {
        for (int start = 0; start < hours.size(); start++) {
            if (ratingOf(hours.get(start), p) != wanted) continue;
            int end = start;
            while (end < hours.size() && ratingOf(hours.get(end), p) == wanted) {
                end++;
            }
            WeatherResponse first = hours.get(start);
            return new BestTime(first.observedAt(), hours.get(end - 1).observedAt().plusHours(1),
                    wanted, first.temperature());
        }
        return null;
    }

    private static Rating ratingOf(WeatherResponse hour, WeatherRatingProperties p) {
        return WeatherRating.rate(hour, p).rating();
    }
}
