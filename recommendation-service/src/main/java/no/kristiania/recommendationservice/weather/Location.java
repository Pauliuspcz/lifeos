package no.kristiania.recommendationservice.weather;

public record Location(double lat, double lon) {

    public static Location rounded(double lat, double lon) {
        return new Location(Math.round(lat * 10) / 10.0, Math.round(lon * 10) / 10.0);
    }
}
