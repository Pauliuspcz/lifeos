package no.kristiania.reportservice.report;

import lombok.extern.slf4j.Slf4j;
import no.kristiania.reportservice.config.RabbitConfig;
import no.kristiania.reportservice.report.events.ProgressCreatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProgressEventListener {
    private final ProgressRecordRepo progressRecordRepo;
    public ProgressEventListener(ProgressRecordRepo progressRecordRepo) {
        this.progressRecordRepo = progressRecordRepo;
    }
    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onProgressCreated(ProgressCreatedEvent event) {
        if(progressRecordRepo.existsByEventId(event.eventId())){
            log.info("Skipping duplicate event {}", event.eventId());
            return;
        }
        ProgressRecord entry = new ProgressRecord();
        entry.setEventId(event.eventId());
        entry.setProgressId(event.progressId());
        entry.setUserId(event.userId());
        entry.setGoalId(event.goalId());
        entry.setDate(event.date());
        entry.setAmount(event.amount());
        progressRecordRepo.save(entry);
        log.info("Saved progress {} for goal {}", event.progressId(), event.goalId());
    }
}
