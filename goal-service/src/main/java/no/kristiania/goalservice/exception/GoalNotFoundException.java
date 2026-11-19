package no.kristiania.goalservice.exception;

import org.springframework.http.HttpStatus;

public class GoalNotFoundException extends ApiException {

    public GoalNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "Goal with id " + id + " not found");
    }
}