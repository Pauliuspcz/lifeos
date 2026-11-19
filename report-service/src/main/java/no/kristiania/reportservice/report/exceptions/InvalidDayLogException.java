package no.kristiania.reportservice.report.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDayLogException extends RuntimeException {
    public InvalidDayLogException(String message) {
        super(message);
    }
}
