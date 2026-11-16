package no.kristiania.recommendationservice.weather.enums;

public enum Rating {
    GOOD, OK, POOR;

    public boolean isBetterThan(Rating other) {
        return ordinal() < other.ordinal();
    }
}
