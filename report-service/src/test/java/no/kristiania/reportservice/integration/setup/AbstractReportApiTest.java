package no.kristiania.reportservice.integration.setup;

import org.springframework.test.web.servlet.ResultActions;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static no.kristiania.reportservice.integration.setup.JwtTestSupport.bearer;
import static no.kristiania.reportservice.integration.setup.JwtTestSupport.tokenFor;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public abstract class AbstractReportApiTest extends AbstractIntegrationTest {

    private static final AtomicLong PROGRESS_IDS = new AtomicLong();

    protected ResultActions getReport(long userId, long goalId) throws Exception {
        return mockMvc.perform(get("/api/reports/goals/{goalId}", goalId)
                .header(AUTHORIZATION, bearer(tokenFor(userId))));
    }

    protected void insertRecord(long userId, long goalId, LocalDate date) {
        jdbcTemplate.update("""
                INSERT INTO progress_record (id, event_id, progress_id, user_id, goal_id, date, received_at)
                VALUES (nextval('progress_record_seq'), ?, ?, ?, ?, ?, now())""",
                UUID.randomUUID(), PROGRESS_IDS.incrementAndGet(), userId, goalId, Date.valueOf(date));
    }
}
