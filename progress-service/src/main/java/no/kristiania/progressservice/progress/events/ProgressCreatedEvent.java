package no.kristiania.progressservice.progress.events;

import no.kristiania.progressservice.client.TrackingModel;
import no.kristiania.progressservice.progress.Progress;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProgressCreatedEvent(
        UUID eventId, Long progressId, Long userId, Long goalId,
        LocalDate date, BigDecimal amount,
        TrackingModel trackingModel, Integer targetCount, String period,
        Instant occurredAt) {
    public static ProgressCreatedEvent from(Progress p, int targetCount, String period) {
        return new ProgressCreatedEvent(p.getEventId(), p.getId(), p.getUserId(),
                p.getGoalId(), p.getDate(), p.getAmount(),
                p.getTrackingModel(), targetCount, period, p.getCreatedAt());
    }
}
