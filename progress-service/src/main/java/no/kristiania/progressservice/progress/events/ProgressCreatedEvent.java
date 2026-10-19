package no.kristiania.progressservice.progress.events;

import no.kristiania.progressservice.progress.Progress;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProgressCreatedEvent (
    UUID eventId,
    Long progressId,
    Long userId,
    Long goalId,
    LocalDate date,
    BigDecimal amount,
    Instant occurredAt
    ){
    public static ProgressCreatedEvent from(Progress p){
        return new ProgressCreatedEvent(p.getEventId(), p.getId(), p.getUserId(),
                p.getGoalId(), p.getDate(), p.getAmount(), p.getCreatedAt());
    }
}
