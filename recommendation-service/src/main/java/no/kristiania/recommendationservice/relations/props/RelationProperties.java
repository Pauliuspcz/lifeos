package no.kristiania.recommendationservice.relations.props;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.relations")
public record RelationProperties(List<RelationRule> rules, Overload overload) {
    public RelationProperties {
        rules = rules == null ? List.of() : List.copyOf(rules);
        overload = overload == null ? new Overload(8) : overload;
    }
    public record Overload(int maxDailyGoals) {}
}
