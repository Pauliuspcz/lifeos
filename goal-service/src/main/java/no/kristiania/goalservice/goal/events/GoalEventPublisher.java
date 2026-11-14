package no.kristiania.goalservice.goal.events;

import lombok.extern.slf4j.Slf4j;
import no.kristiania.goalservice.config.RabbitConfig;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class GoalEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public GoalEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutdoorChanged(GoalOutdoorChangedEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_KEY_OUTDOOR_CHANGED, event);
            log.info("Published outdoor={} for goal {}", event.outdoor(), event.goalId());
        } catch (AmqpException e) {
            log.error("Could not publish GoalOutdoorChangedEvent {}", event.eventId(), e);
        }
    }
}
