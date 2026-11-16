package no.kristiania.reportservice.integration.report;

import no.kristiania.reportservice.integration.setup.AbstractReportApiTest;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

class ActivityReadTest extends AbstractReportApiTest {

    private static final long USER = 1L;

    @Test
    void get_returnsCountsAcrossGoals() throws Exception {
        LocalDate today = LocalDate.now();
        insertRecord(USER, 10L, today);
        insertRecord(USER, 20L, today);
        insertRecord(USER, 10L, today.minusDays(1));

        getActivity(USER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCheckins").value(3))
                .andExpect(jsonPath("$.activeDays").value(2))
                .andExpect(jsonPath("$.days[-1].date").value(today.toString()))
                .andExpect(jsonPath("$.days[-1].count").value(2))
                .andExpect(jsonPath("$.days[-1].level").value(2));
    }

    @Test
    void get_noCheckIns_returnsFullYearOfEmptyDays() throws Exception {
        int expectedDays = 52 * 7 + LocalDate.now().getDayOfWeek().getValue();

        getActivity(USER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCheckins").value(0))
                .andExpect(jsonPath("$.days.length()").value(expectedDays));
    }

    @Test
    void get_sameData_returns304() throws Exception {
        insertRecord(USER, 10L, LocalDate.now());
        String etag = getActivity(USER)
                .andExpect(status().isOk())
                .andExpect(header().string(CACHE_CONTROL, "no-cache, private"))
                .andReturn().getResponse().getHeader(ETAG);

        getActivity(USER, etag).andExpect(status().isNotModified());
    }

    @Test
    void get_afterNewCheckIn_returns200() throws Exception {
        String etag = getActivity(USER).andReturn().getResponse().getHeader(ETAG);
        insertRecord(USER, 10L, LocalDate.now());

        getActivity(USER, etag)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCheckins").value(1));
    }
}
