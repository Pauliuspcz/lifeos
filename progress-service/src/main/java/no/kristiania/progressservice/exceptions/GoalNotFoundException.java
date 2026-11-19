package no.kristiania.progressservice.exceptions;

import org.springframework.http.HttpStatus;

public class GoalNotFoundException extends ApiException {
    public GoalNotFoundException(Long goalId) {
        super(HttpStatus.NOT_FOUND, "goal with id " + goalId + " not found");
    }
}