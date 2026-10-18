package no.kristiania.progressservice.progress;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.progressservice.config.RabbitConfig;
import no.kristiania.progressservice.progress.dto.CreateProgressRequest;
import no.kristiania.progressservice.progress.events.ProgressCreatedEvent;
import no.kristiania.progressservice.progress.exceptions.ProgressNotFoundException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.util.List;


@Slf4j
@Service
public class ProgressService {
    private static final Long TEMP_USER_ID = 1L;
    private final ProgressRepo progressRepo;
    private final RabbitTemplate rabbitTemplate;
    public ProgressService(ProgressRepo progressRepo, RabbitTemplate rabbitTemplate) {
        this.progressRepo = progressRepo;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Progress create(CreateProgressRequest request) {
        Progress progress = new Progress();
        progress.setGoalId(request.goalId());
        progress.setDate(request.date());
        progress.setAmount(request.amount());
        progress.setUserId(TEMP_USER_ID);
        //TODO: check goal belongs to user
        Progress saved = progressRepo.save(progress);
        try{
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_KEY_CREATED, ProgressCreatedEvent.from(saved));
        } catch(AmqpException e) {
            log.error("Could not publish ProgressCreatedEvent {}", saved.getEventId(), e);
        }
        return saved;
    }

    public List<Progress> findAll(Long goalId) {
        if(goalId == null){
            return progressRepo.findByUserIdOrderByDateDesc(TEMP_USER_ID);
        }
        return progressRepo.findByGoalIdAndUserIdOrderByDateDesc(goalId, TEMP_USER_ID);
    }
    public void delete(Long id) {
        Progress progress = progressRepo.findByIdAndUserId(id, TEMP_USER_ID)
                .orElseThrow(() -> new ProgressNotFoundException(id));
        progressRepo.delete(progress);
    }

}
