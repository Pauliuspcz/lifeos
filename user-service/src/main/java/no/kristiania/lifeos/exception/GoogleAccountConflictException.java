package no.kristiania.lifeos.exception;

import org.springframework.http.HttpStatus;

public class GoogleAccountConflictException extends ApiException {
    public GoogleAccountConflictException() { super(HttpStatus.CONFLICT, "Account linked to another Google user"); }
}
