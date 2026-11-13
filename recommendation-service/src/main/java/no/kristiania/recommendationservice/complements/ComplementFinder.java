package no.kristiania.recommendationservice.complements;

import lombok.NoArgsConstructor;
import no.kristiania.recommendationservice.complements.dto.ComplementSuggestion;
import no.kristiania.recommendationservice.complements.props.ComplementRulesProperties;

import java.util.*;

@NoArgsConstructor
public final class ComplementFinder {

    public static List<ComplementSuggestion> find(List<String> categories, ComplementRulesProperties p) {
        Set<String> chosen = new LinkedHashSet<>(categories);
        Map<String, ComplementSuggestion> suggestions = new LinkedHashMap<>();

        for (String category : chosen) {
            for (ComplementRule rule : p.rules().getOrDefault(category, List.of())) {
                String suggested = rule.suggestedCategory();
                if (chosen.contains(suggested)) {
                    continue;
                }
                ComplementSuggestion existing = suggestions.get(suggested);
                if (existing == null) {
                    suggestions.put(suggested, new ComplementSuggestion(suggested, rule.reason(), List.of(category)));
                } else {
                    suggestions.put(suggested, existing.merge(category, rule.reason()));
                }
            }
        }
        return List.copyOf(suggestions.values());
    }
}
