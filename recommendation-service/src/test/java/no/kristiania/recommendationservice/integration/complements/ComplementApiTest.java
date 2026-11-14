package no.kristiania.recommendationservice.integration.complements;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import no.kristiania.recommendationservice.integration.setup.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;


class ComplementApiTest extends AbstractIntegrationTest {

    private static final String TRAINING_AND_LEARNING = """
            {"categories": ["TRAINING", "LEARNING"]}""";

    private static final String ONLY_UNKNOWN = """
            {"categories": ["UNKNOWN"]}""";

    @Test
    void post_returnsMergedSuggestionsFromYamlRules() throws Exception {
        postComplements(TRAINING_AND_LEARNING)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[1].suggestedCategory").value("SLEEP"))
                .andExpect(jsonPath("$[1].basedOnCategories[0]").value("TRAINING"))
                .andExpect(jsonPath("$[1].basedOnCategories[1]").value("LEARNING"));
    }

    @Test
    void post_onlyUnknownCategories_returnsEmptyList() throws Exception {
        postComplements(ONLY_UNKNOWN)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void post_missingCategories_returns400() throws Exception {
        postComplements("{}")
                .andExpect(status().isBadRequest());
    }
    @Test
    void post_withoutToken_returns401() throws Exception {
        postComplementsWithoutToken(TRAINING_AND_LEARNING)
                .andExpect(status().isUnauthorized());
    }
}
