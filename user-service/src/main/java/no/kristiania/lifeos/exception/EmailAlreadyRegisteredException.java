package no.kristiania.lifeos.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyRegisteredException extends ApiException {
    public EmailAlreadyRegisteredException() { super(HttpStatus.CONFLICT, "Email already registered"); }
}
