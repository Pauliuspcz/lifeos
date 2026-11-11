package no.kristiania.reportservice.integration.report;

import no.kristiania.reportservice.integration.setup.AbstractReportApiTest;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportReadTest extends AbstractReportApiTest {

    private static final long USER = 1L;
    private static final long GOAL_ID = 10L;

    @Test
    void get_returnsTotalsAndLast30DayCounts() throws Exception {
        LocalDate today = LocalDate.now();
        insertRecord(USER, GOAL_ID, today);
        insertRecord(USER, GOAL_ID, today.minusDays(29));
        insertRecord(USER, GOAL_ID, today.minusDays(30));
        insertRecord(USER, 20L, today);

        getReport(USER, GOAL_ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalId").value(GOAL_ID))
                .andExpect(jsonPath("$.totalSessions").value(3))
                .andExpect(jsonPath("$.lastDate").value(today.toString()))
                .andExpect(jsonPath("$.last30Days").value(2))
                .andExpect(jsonPath("$.previous30Days").value(1));
    }

    @Test
    void get_goalWithoutCheckIns_returnsEmptyReport() throws Exception {
        getReport(USER, GOAL_ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSessions").value(0))
                .andExpect(jsonPath("$.lastDate").value(nullValue()))
                .andExpect(jsonPath("$.currentStreak").value(0))
                .andExpect(jsonPath("$.history.length()").value(12))
                .andExpect(jsonPath("$.suggestion.recommendation").value("NONE"));
    }
}
