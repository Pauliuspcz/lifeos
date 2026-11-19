package no.kristiania.progressservice.integration.ownership;

import no.kristiania.progressservice.integration.setup.AbstractProgressApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.jdbc.JdbcTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProgressOwnershipTest extends AbstractProgressApiTest {

    private static final long OWNER = 1L;
    private static final long OTHER = 2L;
    private static final long GOAL_ID = 10L;

    @Test
    void get_otherUser_returnsNoneOfTheirData() throws Exception {
        createProgress(OWNER, GOAL_ID);

        getProgress(OWNER).andExpect(jsonPath("$.length()").value(1));
        getProgress(OTHER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getWindow_otherUser_returnsNoneOfTheirData() throws Exception {
        createProgress(OWNER, GOAL_ID);

        getWindow(OWNER).andExpect(jsonPath("$.entries.length()").value(1));
        getWindow(OTHER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries.length()").value(0));
    }

    @Test
    void delete_otherUsersProgress_returns404AndKeepsIt() throws Exception {
        long id = createProgress(OWNER, GOAL_ID);

        deleteProgress(OTHER, id).andExpect(status().isNotFound());
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "progress")).isEqualTo(1);
    }

    @Test
    void delete_ownProgress_returns204() throws Exception {
        long id = createProgress(OWNER, GOAL_ID);

        deleteProgress(OWNER, id).andExpect(status().isNoContent());
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "progress")).isZero();
    }
}
