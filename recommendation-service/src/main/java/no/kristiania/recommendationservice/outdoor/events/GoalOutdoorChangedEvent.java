package no.kristiania.recommendationservice.outdoor.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoalOutdoorChangedEvent(
        UUID eventId, Long goalId, Long userId, boolean outdoor, Instant occurredAt) {}
