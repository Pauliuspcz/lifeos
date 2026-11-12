package no.kristiania.recommendationservice.weather.enums;

public enum WeatherCondition {
    CLEAR, CLOUDY, FOG, DRIZZLE, FREEZING_RAIN, RAIN, SNOW, THUNDERSTORM, UNKNOWN;

    public static WeatherCondition fromWmoCode(int code) {
        return switch (code) {
            case 0, 1 -> CLEAR;
            case 2, 3 -> CLOUDY;
            case 45, 48 -> FOG;
            case 51, 53, 55 -> DRIZZLE;
            case 56, 57, 66, 67 -> FREEZING_RAIN;
            case 61, 63, 65, 80, 81, 82 -> RAIN;
            case 71, 73, 75, 77, 85, 86 -> SNOW;
            case 95, 96, 99 -> THUNDERSTORM;
            default -> UNKNOWN;
        };
    }
}
