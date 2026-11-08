package no.kristiania.reportservice.integration.messaging;

import no.kristiania.reportservice.integration.setup.AbstractIntegrationTest;
import no.kristiania.reportservice.report.enums.TrackingModel;
import no.kristiania.reportservice.report.events.ProgressCreatedEvent;
import no.kristiania.reportservice.report.events.ProgressDeletedEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static no.kristiania.reportservice.config.RabbitConfig.*;
import static org.awaitility.Awaitility.await;

class ProgressEventMessagingTest extends AbstractIntegrationTest {

    @Test
    void createdEvent_isStoredInReadModel() {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_CREATED, created(5L));

        await().atMost(Duration.ofSeconds(10)).until(() -> countFor(5L) == 1);
    }

    @Test
    void deletedEvent_removesRecord() {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_CREATED, created(6L));
        await().atMost(Duration.ofSeconds(10)).until(() -> countFor(6L) == 1);

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_DELETED,
                new ProgressDeletedEvent(UUID.randomUUID(), 6L, 7L, 33L, Instant.now()));

        await().atMost(Duration.ofSeconds(10)).until(() -> countFor(6L) == 0);
    }

    private static ProgressCreatedEvent created(long progressId) {
        return new ProgressCreatedEvent(UUID.randomUUID(), progressId, 7L, 33L, LocalDate.of(2026, 11, 8),
                null, TrackingModel.WEEKLY_COUNT, 2, "WEEKLY", Instant.now());
    }

    private Integer countFor(long progressId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from progress_record where progress_id = ?", Integer.class, progressId);
    }
}
