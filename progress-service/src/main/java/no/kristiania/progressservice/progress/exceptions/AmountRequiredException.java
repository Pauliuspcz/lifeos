package no.kristiania.progressservice.progress.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AmountRequiredException extends RuntimeException {
    public AmountRequiredException() {
        super("This goal requires an amount");
    }
}
