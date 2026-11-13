package no.kristiania.recommendationservice.complements.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ComplementRequest(@NotNull List<String> categories) {}
