package no.kristiania.goalservice.goal.events;

import no.kristiania.goalservice.goal.Goal;

import java.time.Instant;
import java.util.UUID;

public record GoalOutdoorChangedEvent(
        UUID eventId, Long goalId, Long userId, boolean outdoor, Instant occurredAt) {

    public static GoalOutdoorChangedEvent of(Goal g, boolean outdoor) {
        return new GoalOutdoorChangedEvent(UUID.randomUUID(), g.getId(), g.getUserId(),
                outdoor, Instant.now());
    }
}
