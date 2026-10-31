package no.kristiania.progressservice.progress.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class GoalUnavailableException extends RuntimeException {
    public GoalUnavailableException() { super("Goal service is unavailable"); }
}




