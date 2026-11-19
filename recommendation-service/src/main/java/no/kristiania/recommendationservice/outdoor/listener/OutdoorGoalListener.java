package no.kristiania.recommendationservice.outdoor.listener;

import lombok.extern.slf4j.Slf4j;
import no.kristiania.recommendationservice.config.RabbitConfig;
import no.kristiania.recommendationservice.outdoor.entity.OutdoorGoal;
import no.kristiania.recommendationservice.outdoor.events.GoalOutdoorChangedEvent;
import no.kristiania.recommendationservice.outdoor.repository.OutdoorGoalRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OutdoorGoalListener {
    private final OutdoorGoalRepository outdoorGoalRepository;

    public OutdoorGoalListener(OutdoorGoalRepository outdoorGoalRepository) {
        this.outdoorGoalRepository = outdoorGoalRepository;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_OUTDOOR_CHANGED)
    public void onOutdoorChanged(GoalOutdoorChangedEvent event) {
        if (event.outdoor()) {
            outdoorGoalRepository.save(new OutdoorGoal(event.goalId(), event.userId()));
            log.info("Outdoor goal saved goalId={} userId={}", event.goalId(), event.userId());
        } else {
            outdoorGoalRepository.deleteById(event.goalId());
            log.info("Outdoor goal removed goalId={} userId={}", event.goalId(), event.userId());
        }
    }
}