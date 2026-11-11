package no.kristiania.reportservice.integration.ownership;

import no.kristiania.reportservice.integration.setup.AbstractReportApiTest;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportOwnershipTest extends AbstractReportApiTest {

    private static final long OWNER = 1L;
    private static final long OTHER = 2L;
    private static final long GOAL_ID = 10L;

    @Test
    void get_otherUsersGoal_returnsNoneOfTheirData() throws Exception {
        insertRecord(OWNER, GOAL_ID, LocalDate.now());
        insertRecord(OWNER, GOAL_ID, LocalDate.now().minusDays(1));

        getReport(OWNER, GOAL_ID).andExpect(jsonPath("$.totalSessions").value(2));
        getReport(OTHER, GOAL_ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSessions").value(0))
                .andExpect(jsonPath("$.lastDate").value(nullValue()));
    }
}