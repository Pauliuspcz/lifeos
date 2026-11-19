package no.kristiania.progressservice.exceptions;

import org.springframework.http.HttpStatus;

public class GoalNotActiveException extends ApiException {
    public GoalNotActiveException(Long goalId) {
        super(HttpStatus.CONFLICT, "Goal " + goalId + " is not active");
    }
}