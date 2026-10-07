package no.kristiania.goalservice.goal.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GoalNotFoundException extends RuntimeException {
    public GoalNotFoundException(Long id) {
        super("goal with id " + id + " not found");
    }
}
