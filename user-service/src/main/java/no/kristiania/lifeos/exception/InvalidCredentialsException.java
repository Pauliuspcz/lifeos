package no.kristiania.lifeos.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {
    public InvalidCredentialsException() { super(HttpStatus.UNAUTHORIZED, "Invalid credentials"); }
}
