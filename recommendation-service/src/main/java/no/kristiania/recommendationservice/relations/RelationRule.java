package no.kristiania.recommendationservice.relations;

import java.util.List;

public record RelationRule(RelationType type, List<String> a, List<String> b, String reason) {
    public RelationRule {
        a = a == null ? List.of() : List.copyOf(a);
        b = b == null ? List.of() : List.copyOf(b);
    }
}
