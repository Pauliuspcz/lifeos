package no.kristiania.recommendationservice.outdoor;

import lombok.extern.slf4j.Slf4j;
import no.kristiania.recommendationservice.config.RabbitConfig;
import no.kristiania.recommendationservice.outdoor.events.GoalOutdoorChangedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OutdoorGoalListener {
    private final OutdoorGoalRepo outdoorGoalRepo;

    public OutdoorGoalListener(OutdoorGoalRepo outdoorGoalRepo) {
        this.outdoorGoalRepo = outdoorGoalRepo;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_OUTDOOR_CHANGED)
    public void onOutdoorChanged(GoalOutdoorChangedEvent event) {
        if (event.outdoor()) {
            outdoorGoalRepo.save(new OutdoorGoal(event.goalId(), event.userId()));
            log.info("Saved outdoor goal {} for user {}", event.goalId(), event.userId());
        } else {
            outdoorGoalRepo.deleteById(event.goalId());
            log.info("Removed outdoor goal {}", event.goalId());
        }
    }
}
