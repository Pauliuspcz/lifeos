package no.kristiania.recommendationservice.unit.complements;

import no.kristiania.recommendationservice.complements.ComplementFinder;
import no.kristiania.recommendationservice.complements.ComplementRule;
import no.kristiania.recommendationservice.complements.dto.ComplementSuggestion;
import no.kristiania.recommendationservice.complements.props.ComplementRulesProperties;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ComplementFinderTest {
    private static final ComplementRulesProperties PROPS = new ComplementRulesProperties(Map.of(
            "TRAINING", List.of(new ComplementRule("FOOD_DRINK", "Food for training."),
                    new ComplementRule("SLEEP", "Sleep for training.")),
            "SLEEP", List.of(new ComplementRule("MIND", "Mind for sleep.")),
            "MIND", List.of(new ComplementRule("SLEEP", "Sleep for mind.")),
            "FOOD_DRINK", List.of(new ComplementRule("SLEEP", "Sleep for food.")),
            "LEARNING", List.of(new ComplementRule("SLEEP", "Sleep for learning."),
                    new ComplementRule("MIND", "Mind for learning."))));

    private static List<ComplementSuggestion> find(String... categories) {
        return ComplementFinder.find(List.of(categories), PROPS);
    }

    private static List<String> suggested(String... categories) {
        return find(categories).stream().map(ComplementSuggestion::suggestedCategory).toList();
    }

    @Test
    void trainingSuggestsFoodDrinkAndSleep() {
        assertThat(suggested("TRAINING")).containsExactly("FOOD_DRINK", "SLEEP");
    }

    @Test
    void sleepSuggestsMind() {
        assertThat(suggested("SLEEP")).containsExactly("MIND");
    }

    @Test
    void mindSuggestsSleep() {
        assertThat(suggested("MIND")).containsExactly("SLEEP");
    }

    @Test
    void foodDrinkSuggestsSleep() {
        assertThat(suggested("FOOD_DRINK")).containsExactly("SLEEP");
    }

    @Test
    void learningSuggestsSleepAndMind() {
        assertThat(suggested("LEARNING")).containsExactly("SLEEP", "MIND");
    }

    @Test
    void socialSuggestsNothing() {
        assertThat(suggested("SOCIAL")).isEmpty();
    }

    @Test
    void alreadyChosenCategoryIsNotSuggested() {
        assertThat(suggested("TRAINING", "SLEEP")).containsExactly("FOOD_DRINK", "MIND");
    }

    @Test
    void sameSuggestionFromTwoCategoriesIsMerged() {
        assertThat(suggested("TRAINING", "LEARNING")).containsExactly("FOOD_DRINK", "SLEEP", "MIND");

        ComplementSuggestion sleep = find("TRAINING", "LEARNING").get(1);
        assertThat(sleep.basedOnCategories()).containsExactly("TRAINING", "LEARNING");
        assertThat(sleep.reason()).isEqualTo("Sleep for training. Sleep for learning.");
    }

    @Test
    void unknownCodeIsIgnored() {
        assertThat(suggested("UNKNOWN", "SLEEP")).containsExactly("MIND");
    }

    @Test
    void emptyListReturnsEmpty() {
        assertThat(suggested()).isEmpty();
    }
}
