package no.kristiania.goalservice.goal.exceptions;

import org.springframework.http.HttpStatus;

public class GoalNotArchivedException extends ApiException {
    public GoalNotArchivedException() {
        super(HttpStatus.CONFLICT, "Goal must be archived before it can be deleted");
    }
}
