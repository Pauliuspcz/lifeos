package no.kristiania.reportservice.report.events;

import no.kristiania.reportservice.report.enums.TrackingModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProgressCreatedEvent(
        UUID eventId,
        Long progressId,
        Long userId,
        Long goalId,
        LocalDate date,
        BigDecimal amount,
        TrackingModel trackingModel,
        Integer targetCount,
        Instant occurredAt
) {
}
