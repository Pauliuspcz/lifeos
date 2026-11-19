package no.kristiania.progressservice.exceptions;

import org.springframework.http.HttpStatus;

public class AmountRequiredException extends ApiException {
    public AmountRequiredException() {
        super(HttpStatus.BAD_REQUEST, "This goal requires an amount");
    }
}