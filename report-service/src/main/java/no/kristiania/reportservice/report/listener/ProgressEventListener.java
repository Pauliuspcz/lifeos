package no.kristiania.reportservice.report.listener;

import lombok.extern.slf4j.Slf4j;
import no.kristiania.reportservice.config.RabbitConfig;
import no.kristiania.reportservice.report.entity.ProgressRecord;
import no.kristiania.reportservice.report.repository.ProgressRecordRepository;
import no.kristiania.reportservice.report.events.ProgressCreatedEvent;
import no.kristiania.reportservice.report.events.ProgressDeletedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProgressEventListener {
    private final ProgressRecordRepository progressRecordRepository;

    public ProgressEventListener(ProgressRecordRepository progressRecordRepository) {
        this.progressRecordRepository = progressRecordRepository;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_CREATED)
    public void onProgressCreated(ProgressCreatedEvent event) {
        if (progressRecordRepository.existsByEventId(event.eventId())) {
            log.info("Duplicate progress event skipped eventId={} progressId={}",
                    event.eventId(), event.progressId());
            return;
        }
        ProgressRecord entry = new ProgressRecord();
        entry.setEventId(event.eventId());
        entry.setProgressId(event.progressId());
        entry.setUserId(event.userId());
        entry.setGoalId(event.goalId());
        entry.setDate(event.date());
        entry.setAmount(event.amount());
        entry.setTrackingModel(event.trackingModel());
        entry.setTargetCount(event.targetCount());
        entry.setPeriod(event.period());
        progressRecordRepository.save(entry);
        log.info("Progress record saved progressId={} userId={} goalId={}",
                event.progressId(), event.userId(), event.goalId());
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_DELETED)
    public void onProgressDeleted(ProgressDeletedEvent event) {
        progressRecordRepository.deleteByProgressId(event.progressId());
        log.info("Progress delete event handled progressId={} userId={} goalId={}",
                event.progressId(), event.userId(), event.goalId());
    }
}