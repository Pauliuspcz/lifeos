package no.kristiania.progressservice.exceptions;

import org.springframework.http.HttpStatus;

public class UnsupportedTrackingException extends ApiException {
    public UnsupportedTrackingException(String message) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, message);
    }
}