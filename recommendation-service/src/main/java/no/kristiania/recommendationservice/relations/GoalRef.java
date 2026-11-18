package no.kristiania.recommendationservice.relations;

public record GoalRef(String templateCode, String directionCode, String period) {
    public boolean matches(String code) {
        return code != null && (code.equals(templateCode) || code.equals(directionCode));
    }
}
