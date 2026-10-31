package no.kristiania.progressservice.progress.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class UnsupportedTrackingException extends RuntimeException {
    public UnsupportedTrackingException(String type) { super("Unsupported trackingType: " + type); }
}