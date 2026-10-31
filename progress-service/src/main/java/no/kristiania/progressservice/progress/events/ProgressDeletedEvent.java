package no.kristiania.progressservice.progress.events;

import no.kristiania.progressservice.client.TrackingModel;
import no.kristiania.progressservice.progress.Progress;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProgressDeletedEvent(
        UUID eventId, Long progressId, Long userId, Long goalId,
        LocalDate date, BigDecimal amount, TrackingModel trackingModel,
        Instant occurredAt) {
    public static ProgressDeletedEvent from(Progress p) {
        return new ProgressDeletedEvent(UUID.randomUUID(), p.getId(), p.getUserId(),
                p.getGoalId(), p.getDate(), p.getAmount(),
                p.getTrackingModel(), Instant.now());
    }
}
