package no.kristiania.progressservice.progress.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;

@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class LogDateOutsideWindowException extends RuntimeException {
    public LogDateOutsideWindowException(LocalDate from, LocalDate to) {
        super("Date must be between " + from + " and " + to);
    }
}
