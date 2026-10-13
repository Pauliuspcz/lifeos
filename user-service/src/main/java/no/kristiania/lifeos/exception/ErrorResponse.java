package no.kristiania.lifeos.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(int status, String error, String message,
                            Instant timestamp, Map<String, String> fieldErrors) {

    public static ErrorResponse of(HttpStatus status, String message) {
        return new ErrorResponse(status.value(), status.getReasonPhrase(), message, Instant.now(), null);
    }
}
