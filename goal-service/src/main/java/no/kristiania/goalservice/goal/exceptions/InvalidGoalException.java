package no.kristiania.goalservice.goal.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidGoalException extends ApiException {
    public InvalidGoalException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
