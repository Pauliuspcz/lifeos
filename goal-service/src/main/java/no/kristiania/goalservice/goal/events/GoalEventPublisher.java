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
            log.info("Outdoor changed event published goalId={} userId={} outdoor={} eventId={}",
                    event.goalId(), event.userId(), event.outdoor(), event.eventId());
        } catch (AmqpException e) {
            log.error("Outdoor changed event publish failed goalId={} eventId={}",
                    event.goalId(), event.eventId(), e);
        }
    }
}