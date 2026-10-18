package no.kristiania.progressservice.progress.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProgressNotFoundException extends RuntimeException {
    public ProgressNotFoundException(Long id) {
        super("progress with id " + id + " not found");
    }
}
