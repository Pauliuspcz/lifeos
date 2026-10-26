package no.kristiania.progressservice.progress;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.progressservice.client.GoalClient;
import no.kristiania.progressservice.config.RabbitConfig;
import no.kristiania.progressservice.progress.dto.CreateProgressRequest;
import no.kristiania.progressservice.progress.events.ProgressCreatedEvent;
import no.kristiania.progressservice.progress.events.ProgressDeletedEvent;
import no.kristiania.progressservice.progress.exceptions.GoalNotFoundException;
import no.kristiania.progressservice.progress.exceptions.ProgressNotFoundException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@Slf4j
@Service
public class ProgressService {
    private final ProgressRepo progressRepo;
    private final RabbitTemplate rabbitTemplate;
    private final GoalClient goalClient;
    public ProgressService(ProgressRepo progressRepo, RabbitTemplate rabbitTemplate, GoalClient goalClient) {
        this.progressRepo = progressRepo;
        this.rabbitTemplate = rabbitTemplate;
        this.goalClient = goalClient;
    }

    public Progress create(Long userId, CreateProgressRequest request) {
        try{
            goalClient.getGoal(request.goalId());
        }catch(HttpClientErrorException.NotFound e){
            throw new GoalNotFoundException(request.goalId());
        } catch(ResourceAccessException e){
            log.error("goal-service is down");
        }
        Progress progress = new Progress();
        progress.setGoalId(request.goalId());
        progress.setDate(request.date());
        progress.setAmount(request.amount());
        progress.setUserId(userId);
        Progress saved = progressRepo.save(progress);
        try{
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_KEY_CREATED, ProgressCreatedEvent.from(saved));
        } catch(AmqpException e) {
            log.error("Could not publish ProgressCreatedEvent {}", saved.getEventId(), e);
        }
        return saved;
    }

    public List<Progress> findAll(Long userId, Long goalId) {
        if(goalId == null){
            return progressRepo.findByUserIdOrderByDateDesc(userId);
        }
        return progressRepo.findByGoalIdAndUserIdOrderByDateDesc(goalId, userId);
    }
    public void delete(Long userId, Long id) {
        Progress progress = progressRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ProgressNotFoundException(id));
        progressRepo.delete(progress);
        try{
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_KEY_DELETED, ProgressDeletedEvent.from(progress));
        }catch (AmqpException e) {
            log.error("Could not publish ProgressDeletedEvent for progress {}", progress.getId(), e);
        }
    }

}
