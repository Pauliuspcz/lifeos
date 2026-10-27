package no.kristiania.goalservice.goal.exceptions;

import org.springframework.http.HttpStatus;

public class GoalArchivedException extends ApiException {
    public GoalArchivedException() {
        super(HttpStatus.CONFLICT, "Archived goal cannot be updated");
    }
}
