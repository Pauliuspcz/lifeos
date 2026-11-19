package no.kristiania.recommendationservice.relations.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RelationRequest(@NotNull List<@Valid GoalRef> goals, @Valid GoalRef candidate) {}

