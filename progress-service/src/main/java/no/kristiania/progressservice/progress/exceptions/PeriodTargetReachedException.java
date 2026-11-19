package no.kristiania.progressservice.progress.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PeriodTargetReachedException extends RuntimeException {
    public PeriodTargetReachedException() { super("Target already reached for this period"); }
}
