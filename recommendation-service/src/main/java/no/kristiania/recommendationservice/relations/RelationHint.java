package no.kristiania.recommendationservice.relations;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record RelationHint(RelationType type, List<String> involved, String suggestion, String reason) {
    RelationHint merge(RelationHint other) {
        Set<String> all = new LinkedHashSet<>(involved);
        all.addAll(other.involved());
        return new RelationHint(type, List.copyOf(all), suggestion, reason);
    }
}

