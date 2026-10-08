package no.kristiania.progressservice.progress;
import no.kristiania.progressservice.progress.dto.CreateProgressRequest;
import org.springframework.stereotype.Service;


@Service
public class ProgressService {
    private static final Long TEMP_USER_ID = 1L;
    private final ProgressRepo progressRepo;
    public ProgressService(ProgressRepo progressRepo) {
        this.progressRepo = progressRepo;
    }

    public Progress create(CreateProgressRequest request) {
        Progress progress = new Progress();
        progress.setGoalId(request.goalId());
        progress.setDate(request.date());
        progress.setAmount(request.amount());
        progress.setUserId(TEMP_USER_ID);
        //TODO: check goal belongs to user
        // TODO: publish progressCreated event with eventId to RabbitMQ
        return progressRepo.save(progress);
    }

}
