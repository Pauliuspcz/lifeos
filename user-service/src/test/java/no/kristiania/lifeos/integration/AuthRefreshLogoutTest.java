package no.kristiania.lifeos.integration;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthRefreshLogoutTest extends AbstractIntegrationTest {

    @Test
    void refresh_returnsNewPair_andRevokesTheOldToken() throws Exception {
        Tokens first = registerAndLogin("kari@example.com");

        Tokens second = tokensFrom(refresh(first.refresh())
                .andExpect(status().isOk()).andReturn());

        assertThat(second.refresh()).isNotEqualTo(first.refresh());
        assertThat(second.access()).isNotBlank();
    }

    @Test
    void refresh_reuseWithinGracePeriod_returns401_butKeepsTheNewSession() throws Exception {
        Tokens first = registerAndLogin("kari@example.com");
        Tokens second = tokensFrom(refresh(first.refresh()).andExpect(status().isOk()).andReturn());

        refresh(first.refresh()).andExpect(status().isUnauthorized());

        refresh(second.refresh()).andExpect(status().isOk());
    }

    @Test
    void refresh_reuseAfterGracePeriod_returns401_andRevokesAllTokensForUser() throws Exception {
        Tokens first = registerAndLogin("kari@example.com");
        Tokens second = tokensFrom(refresh(first.refresh()).andExpect(status().isOk()).andReturn());
        int updated = jdbcTemplate.update(
                "update refresh_tokens set revoked_at = now() - interval '1 day' where token_hash = ?",
                sha256Hex(first.refresh()));
        assertThat(updated).isEqualTo(1);

        refresh(first.refresh()).andExpect(status().isUnauthorized());

        refresh(second.refresh()).andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_withUnknownToken_returns401() throws Exception {
        refresh("this-token-does-not-exist").andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_withExpiredToken_returns401() throws Exception {
        Tokens tokens = registerAndLogin("kari@example.com");
        int updated = jdbcTemplate.update(
                "update refresh_tokens set expires_at = now() - interval '1 day'");
        assertThat(updated).isEqualTo(1);
        refresh(tokens.refresh()).andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_withMissingToken_returns400() throws Exception {
        postJson("/auth/refresh", "{}").andExpect(status().isBadRequest());
    }

    @Test
    void refresh_storesOnlyHash_notTheTokenItself() throws Exception {
        Tokens tokens = registerAndLogin("kari@example.com");

        Integer plainMatches = jdbcTemplate.queryForObject(
                "select count(*) from refresh_tokens where token_hash = ?", Integer.class, tokens.refresh());

        assertThat(plainMatches).isZero();
    }

    @Test
    void logout_revokesToken_soRefreshFails() throws Exception {
        Tokens tokens = registerAndLogin("kari@example.com");

        postJson("/auth/logout", """
                {"refreshToken":"%s"}""".formatted(tokens.refresh()))
                .andExpect(status().isNoContent());

        refresh(tokens.refresh()).andExpect(status().isUnauthorized());
    }

    @Test
    void logout_withUnknownToken_returns204() throws Exception {
        postJson("/auth/logout", """
                {"refreshToken":"unknown"}""")
                .andExpect(status().isNoContent());
    }

    @RepeatedTest(10)
    void refresh_withSameTokenUsedConcurrently_succeedsExactlyOnce() throws Exception {
        Tokens tokens = registerAndLogin("kari@example.com");
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CyclicBarrier startTogether = new CyclicBarrier(threads);

        List<Future<Integer>> calls = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            calls.add(executor.submit(() -> {
                startTogether.await();
                return refresh(tokens.refresh()).andReturn().getResponse().getStatus();
            }));
        }

        List<Integer> statuses = new ArrayList<>();
        for (Future<Integer> call : calls) {
            statuses.add(call.get(30, TimeUnit.SECONDS));
        }
        executor.shutdown();

        assertThat(statuses).filteredOn(status -> status == 200).hasSize(1);
        assertThat(statuses).filteredOn(status -> status == 401).hasSize(threads - 1);
        Integer rows = jdbcTemplate.queryForObject(
                "select count(*) from refresh_tokens", Integer.class);
        Integer active = jdbcTemplate.queryForObject(
                "select count(*) from refresh_tokens where revoked = false", Integer.class);
        assertThat(rows).isEqualTo(2);
        assertThat(active).isEqualTo(1);
    }

}
