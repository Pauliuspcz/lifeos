package no.kristiania.recommendationservice.complements.props;

import no.kristiania.recommendationservice.complements.ComplementRule;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "app.complements")
public record ComplementRulesProperties(
        Map<String, List<ComplementRule>> rules
) {}
