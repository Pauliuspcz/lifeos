package no.kristiania.recommendationservice.unit.relations;

import no.kristiania.recommendationservice.relations.dto.GoalRef;
import no.kristiania.recommendationservice.relations.dto.RelationHint;
import no.kristiania.recommendationservice.relations.enums.RelationType;
import no.kristiania.recommendationservice.relations.props.RelationProperties;
import no.kristiania.recommendationservice.relations.props.RelationRule;
import no.kristiania.recommendationservice.relations.utils.RelationFinder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RelationFinderTest {

    private final RelationProperties props = new RelationProperties(
            List.of(
                    new RelationRule(RelationType.TENSION, List.of("BUILD_ENDURANCE"), List.of("GET_STRONGER"), "tension"),
                    new RelationRule(RelationType.SUPPORTS, List.of("BUILD_ENDURANCE"), List.of("SLEEP_8_HOURS"), "sleep")),
            new RelationProperties.Overload(1));

    private final GoalRef run = new GoalRef("RUN_3X_WEEK", "BUILD_ENDURANCE", "WEEKLY");
    private final GoalRef lift = new GoalRef("STRENGTH_2X_WEEK", "GET_STRONGER", "WEEKLY");
    private final GoalRef sleep = new GoalRef("SLEEP_8_HOURS", "SLEEP_BETTER", "DAILY");

    @Test
    void tensionIsFoundInEitherOrder() {
        assertThat(RelationFinder.find(List.of(run, lift), null, props))
                .extracting(RelationHint::type).contains(RelationType.TENSION);
        assertThat(RelationFinder.find(List.of(lift, run), null, props))
                .extracting(RelationHint::type).contains(RelationType.TENSION);
    }

    @Test
    void suggestsMissingGoalAndPairsExistingOne() {
        assertThat(RelationFinder.find(List.of(run), null, props))
                .anyMatch(h -> "SLEEP_8_HOURS".equals(h.suggestion()));
        assertThat(RelationFinder.find(List.of(run, sleep), null, props))
                .anyMatch(h -> h.type() == RelationType.SUPPORTS && h.suggestion() == null);
    }

    @Test
    void candidateModeOnlyReturnsHintsAboutTheCandidate() {
        assertThat(RelationFinder.find(List.of(run), lift, props))
                .isNotEmpty()
                .allMatch(h -> h.involved().contains("STRENGTH_2X_WEEK") || h.type() == RelationType.OVERLOAD);
    }

    @Test
    void overloadOnlyAboveTheLimit() {
        GoalRef first = new GoalRef("A", null, "DAILY");
        GoalRef second = new GoalRef("B", null, "DAILY");
        assertThat(RelationFinder.find(List.of(first), null, props)).isEmpty();
        assertThat(RelationFinder.find(List.of(first, second), null, props))
                .extracting(RelationHint::type).contains(RelationType.OVERLOAD);
    }
}
