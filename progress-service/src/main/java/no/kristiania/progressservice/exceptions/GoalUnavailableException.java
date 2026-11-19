package no.kristiania.progressservice.exceptions;

import org.springframework.http.HttpStatus;

public class GoalUnavailableException extends ApiException {
    public GoalUnavailableException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, "Goal service is unavailable");
    }
}