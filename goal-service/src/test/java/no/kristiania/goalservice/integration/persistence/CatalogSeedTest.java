package no.kristiania.goalservice.integration.persistence;

import no.kristiania.goalservice.catalog.*;
import no.kristiania.goalservice.integration.setup.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogSeedTest extends AbstractIntegrationTest {

    @Autowired CategoryRepository categories;
    @Autowired DirectionRepository directions;
    @Autowired GoalTemplateRepository templates;


    @Test
    void directionTemplates_competition_containsMarathon_groupedByCategory() {
        Direction competition = directions.findAllByOrderBySortOrderAsc().stream()
                .filter(d -> d.getCode().equals("COMPETITION")).findFirst().orElseThrow();

        var result = templates.findActiveByDirectionId(competition.getId());

        assertThat(result).extracting(GoalTemplate::getTitle).contains("Run a marathon");
        assertThat(result).hasSize(5);
        assertThat(result).extracting(GoalTemplate::getCategoryId).isSorted();
    }

    @Test
    void categoryTemplates_sleep_returnsThreeTemplatesInOrder() {
        Category sleep = categories.findAllByOrderBySortOrderAsc().stream()
                .filter(c -> c.getCode().equals("SLEEP")).findFirst().orElseThrow();

        var result = templates.findAllByCategoryIdAndActiveTrueOrderBySortOrderAsc(sleep.getId());

        assertThat(result).extracting(GoalTemplate::getTitle)
                .containsExactly("Go to bed before 23:00", "Sleep 8 hours", "No screens after 22:00");
    }
}
