package no.kristiania.lifeos.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthLoginTest extends AbstractIntegrationTest {

    @Test
    void login_withCorrectCredentials_returnsTokenPair() throws Exception {
        register("kari@example.com", PASSWORD);
        postJson("/auth/login", credentials("kari@example.com", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyString())))
                .andExpect(jsonPath("$.refreshToken", not(emptyString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(3600));
    }

    @Test
    void login_isCaseInsensitiveOnEmail() throws Exception {
        register("kari@example.com", PASSWORD);
        postJson("/auth/login", credentials("KARI@example.com", PASSWORD))
                .andExpect(status().isOk());
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        register("kari@example.com", PASSWORD);
        postJson("/auth/login", credentials("kari@example.com", "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void login_withUnknownEmail_returns401() throws Exception {
        postJson("/auth/login", credentials("nobody@example.com", PASSWORD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_wrongPasswordAndUnknownEmail_giveIdenticalErrors() throws Exception {
        register("kari@example.com", PASSWORD);
        String wrongPassword = postJson("/auth/login", credentials("kari@example.com", "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        String unknownEmail = postJson("/auth/login", credentials("nobody@example.com", PASSWORD))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(JsonPath.<String>read(wrongPassword, "$.message"))
                .isEqualTo(JsonPath.read(unknownEmail, "$.message"))
                .isEqualTo("Invalid credentials");
    }

    @Test
    void login_withEmptyBody_returns400() throws Exception {
        postJson("/auth/login", "{}")
                .andExpect(status().isBadRequest());
    }
}
