package no.kristiania.reportservice.integration.report;

import no.kristiania.reportservice.integration.setup.AbstractReportApiTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DayLogTest extends AbstractReportApiTest {

    private static final long USER = 1L;
    private static final long OTHER = 2L;

    @Test
    void put_thenGetToday_returnsSavedValues() throws Exception {
        putDayLog(USER, "{\"mood\":4,\"sleepHours\":7}").andExpect(status().isOk());

        getDayLogToday(USER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mood").value(4))
                .andExpect(jsonPath("$.sleepHours").value(7));
    }

    @Test
    void put_onlyMood_keepsSleep() throws Exception {
        putDayLog(USER, "{\"sleepHours\":8}");
        putDayLog(USER, "{\"mood\":2}");

        getDayLogToday(USER)
                .andExpect(jsonPath("$.mood").value(2))
                .andExpect(jsonPath("$.sleepHours").value(8));
    }

    @Test
    void put_moodOutOfRange_returns400() throws Exception {
        putDayLog(USER, "{\"mood\":9}").andExpect(status().isBadRequest());
    }

    @Test
    void getToday_otherUser_seesNothing() throws Exception {
        putDayLog(USER, "{\"mood\":4}");

        getDayLogToday(OTHER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mood").isEmpty());
    }
}
