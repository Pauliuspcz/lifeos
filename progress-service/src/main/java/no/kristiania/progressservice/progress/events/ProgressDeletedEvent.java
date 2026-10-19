package no.kristiania.progressservice.progress.events;

import no.kristiania.progressservice.progress.Progress;

import java.time.Instant;
import java.util.UUID;

public record ProgressDeletedEvent(
        UUID eventId,
        Long progressId,
        Long userId,
        Long goalId,
        Instant occurredAt
) {
    public static ProgressDeletedEvent from(Progress p){
        return new ProgressDeletedEvent(UUID.randomUUID(), p.getId(), p.getUserId(),
                p.getGoalId(), Instant.now());
    }
}
