package no.kristiania.lifeos.exception;

import org.springframework.http.HttpStatus;

public class InvalidGoogleTokenException extends ApiException {
    public InvalidGoogleTokenException() { super(HttpStatus.UNAUTHORIZED, "Invalid Google token"); }
}