package no.kristiania.goalservice.goal.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(int status, String error, String message, Instant timestamp,
                       Map<String, String> fieldErrors) {
}
