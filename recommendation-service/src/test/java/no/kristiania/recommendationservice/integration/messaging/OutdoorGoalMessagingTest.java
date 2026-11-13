package no.kristiania.recommendationservice.integration.messaging;

import no.kristiania.recommendationservice.integration.setup.AbstractIntegrationTest;
import no.kristiania.recommendationservice.outdoor.events.GoalOutdoorChangedEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static no.kristiania.recommendationservice.config.RabbitConfig.*;
import static org.awaitility.Awaitility.await;

class OutdoorGoalMessagingTest extends AbstractIntegrationTest {

    @Test
    void trueEvent_storesOutdoorGoal() {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_OUTDOOR_CHANGED, event(5L, true));

        await().atMost(Duration.ofSeconds(10)).until(() -> countFor(5L) == 1);
    }

    @Test
    void falseEvent_removesOutdoorGoal() {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_OUTDOOR_CHANGED, event(6L, true));
        await().atMost(Duration.ofSeconds(10)).until(() -> countFor(6L) == 1);

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_OUTDOOR_CHANGED, event(6L, false));

        await().atMost(Duration.ofSeconds(10)).until(() -> countFor(6L) == 0);
    }

    @Test
    void duplicateTrueEvent_isStoredOnce() {
        GoalOutdoorChangedEvent event = event(8L, true);

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_OUTDOOR_CHANGED, event);
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_OUTDOOR_CHANGED, event);

        await().atMost(Duration.ofSeconds(10)).until(() -> countFor(8L) == 1);
        await().during(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(5)).until(() -> countFor(8L) == 1);
    }

    private static GoalOutdoorChangedEvent event(long goalId, boolean outdoor) {
        return new GoalOutdoorChangedEvent(UUID.randomUUID(), goalId, 7L, outdoor, Instant.now());
    }

    private Integer countFor(long goalId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from outdoor_goal where goal_id = ?", Integer.class, goalId);
    }
}
