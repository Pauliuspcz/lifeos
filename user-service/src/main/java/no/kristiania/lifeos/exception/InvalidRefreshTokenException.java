package no.kristiania.lifeos.exception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends ApiException {
    public InvalidRefreshTokenException() { super(HttpStatus.UNAUTHORIZED, "Invalid refresh token"); }
}
