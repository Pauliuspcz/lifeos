package no.kristiania.recommendationservice.complements.dto;

import java.util.ArrayList;
import java.util.List;

public record ComplementSuggestion(String suggestedCategory, String reason, List<String> basedOnCategories) {

    public ComplementSuggestion merge(String category, String otherReason) {
        List<String> based = new ArrayList<>(basedOnCategories);
        based.add(category);
        return new ComplementSuggestion(suggestedCategory, reason + " " + otherReason, based);
    }
}