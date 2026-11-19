package no.kristiania.recommendationservice.relations.dto;

import no.kristiania.recommendationservice.relations.enums.RelationType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record RelationHint(RelationType type, List<String> involved, String suggestion, String reason) {

    public RelationHint merge(RelationHint other) {
        Set<String> all = new LinkedHashSet<>(involved);
        all.addAll(other.involved());
        return new RelationHint(type, List.copyOf(all), suggestion, reason);
    }
}