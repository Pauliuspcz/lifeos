package no.kristiania.goalservice.integration.startup;

import no.kristiania.goalservice.integration.setup.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class StartupTest extends AbstractIntegrationTest {

    @Test
    void allFlywayMigrations_haveRunSuccessfully() {
        Integer applied = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where success = true", Integer.class);
        Integer failed = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where success = false", Integer.class);

        assertThat(applied).isPositive();
        assertThat(failed).isZero();
    }

    @Test
    void goalsTable_exists() {
        Integer tables = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'goals'",
                Integer.class);

        assertThat(tables).isEqualTo(1);
    }
}
