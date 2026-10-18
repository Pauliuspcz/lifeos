package no.kristiania.reportservice.report.events;

import java.time.Instant;
import java.util.UUID;

public record ProgressDeletedEvent(
        UUID eventId,
        Long progressId,
        Long userId,
        Long goalId,
        Instant occurredAt
) {
}
