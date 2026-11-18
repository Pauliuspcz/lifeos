package no.kristiania.progressservice.client;

import no.kristiania.progressservice.progress.exceptions.UnsupportedTrackingException;

public enum TrackingModel {
    DAILY_CHECK, WEEKLY_COUNT, MONTHLY_COUNT, CUMULATIVE_SUM;

    public static TrackingModel from(GoalSummary goal) {
        if (goal.trackingType() == null) {
            throw new UnsupportedTrackingException("Goal has no trackingType");
        }
        return switch (goal.trackingType()) {
            case "AMOUNT" -> CUMULATIVE_SUM;
            case "CHECK" -> switch (String.valueOf(goal.period())) {
                case "DAILY" -> DAILY_CHECK;
                case "WEEKLY" -> WEEKLY_COUNT;
                case "MONTHLY" -> MONTHLY_COUNT;
                default -> throw new UnsupportedTrackingException(
                        "Unsupported period for CHECK: " + goal.period());
            };
            default -> throw new UnsupportedTrackingException(
                    "Unsupported trackingType: " + goal.trackingType());
        };
    }
}
