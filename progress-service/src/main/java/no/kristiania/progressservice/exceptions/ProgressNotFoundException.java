package no.kristiania.progressservice.exceptions;

import org.springframework.http.HttpStatus;

public class ProgressNotFoundException extends ApiException {
    public ProgressNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "progress with id " + id + " not found");
    }
}
