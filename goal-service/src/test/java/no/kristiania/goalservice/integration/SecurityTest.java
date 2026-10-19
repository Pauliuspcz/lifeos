package no.kristiania.goalservice.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SecurityTest extends AbstractIntegrationTest {

    @Test
    void request_withoutToken_returns401_withJsonBody() throws Exception {
        mockMvc.perform(get("/goals"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void request_withGarbageToken_returns401() throws Exception {
        mockMvc.perform(get("/goals").header(AUTHORIZATION, bearer("not.a.jwt")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void request_withExpiredToken_returns401() throws Exception {
        mockMvc.perform(get("/goals").header(AUTHORIZATION, bearer(expiredTokenFor(1L))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void request_withTokenSignedByOtherKey_returns401() throws Exception {
        mockMvc.perform(get("/goals").header(AUTHORIZATION, bearer(tokenSignedWithOtherKey(1L))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void request_withUnsignedToken_returns401() throws Exception {
        mockMvc.perform(get("/goals").header(AUTHORIZATION, bearer(unsignedToken(1L))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void request_withNonNumericSubject_returns401() throws Exception {
        mockMvc.perform(get("/goals").header(AUTHORIZATION, bearer(tokenWithSubject("not-a-number"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void request_withValidToken_isNotRejectedAsUnauthenticated() throws Exception {
        mockMvc.perform(get("/goals").header(AUTHORIZATION, bearer(tokenFor(1L))))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(401));
    }
}
