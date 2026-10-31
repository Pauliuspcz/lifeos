package no.kristiania.progressservice.client;

import no.kristiania.progressservice.progress.exceptions.UnsupportedTrackingException;

//Todo: lowekey midlertidig til jeg har endra seeding osv i goal service
public enum TrackingModel {
    DAILY_CHECK, WEEKLY_COUNT, CUMULATIVE_SUM;

    public static TrackingModel from(GoalSummary goal) {
        if (goal.trackingType() == null) {
            throw new UnsupportedTrackingException("Goal has no trackingType");
        }
        return switch (goal.trackingType()) {
            case "COUNT" -> WEEKLY_COUNT;
            case "CHECK", "AMOUNT", "DURATION", "TIME_TARGET", "LIMIT" -> DAILY_CHECK;
            default -> throw new UnsupportedTrackingException(
                    "Unsupported trackingType: " + goal.trackingType());
        };
    }
}
