package no.kristiania.reportservice.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidDayLogException extends ApiException {
    public InvalidDayLogException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
