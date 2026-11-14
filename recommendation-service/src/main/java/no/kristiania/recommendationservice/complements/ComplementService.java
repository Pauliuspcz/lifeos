package no.kristiania.recommendationservice.complements;

import no.kristiania.recommendationservice.complements.dto.ComplementSuggestion;
import no.kristiania.recommendationservice.complements.props.ComplementRulesProperties;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComplementService {
    private final ComplementRulesProperties complementRulesProperties;

    public ComplementService(ComplementRulesProperties complementRulesProperties) {
        this.complementRulesProperties = complementRulesProperties;
    }

    public List<ComplementSuggestion> getSuggestions(List<String> categories) {
        return ComplementFinder.find(categories, complementRulesProperties);
    }
}
