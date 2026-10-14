package no.kristiania.lifeos.integration;

import org.junit.jupiter.api.Test;

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
    void refresh_withReusedToken_returns401_andRevokesAllTokensForUser() throws Exception {
        Tokens first = registerAndLogin("kari@example.com");
        Tokens second = tokensFrom(refresh(first.refresh())
                .andExpect(status().isOk()).andReturn());

        refresh(first.refresh()).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
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
}
