package no.kristiania.progressservice.exceptions;

import org.springframework.http.HttpStatus;

import java.time.LocalDate;

public class LogDateOutsideWindowException extends ApiException {
    public LogDateOutsideWindowException(LocalDate from, LocalDate to) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, "Date must be between " + from + " and " + to);
    }
}
