package no.kristiania.progressservice.exceptions;

import org.springframework.http.HttpStatus;

public class PeriodTargetReachedException extends ApiException {
    public PeriodTargetReachedException() {
        super(HttpStatus.CONFLICT, "Target already reached for this period");
    }
}
