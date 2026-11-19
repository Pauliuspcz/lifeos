package no.kristiania.goalservice.exception;

import org.springframework.http.HttpStatus;

public class CatalogItemNotFoundException extends ApiException {
    public CatalogItemNotFoundException(String kind, Long id) {
        super(HttpStatus.NOT_FOUND, kind + " with id " + id + " not found");
    }
}
