package no.kristiania.progressservice.progress.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class GoalNotActiveException extends RuntimeException {
    public GoalNotActiveException(Long goalId) { super("Goal " + goalId + " is not active"); }
}