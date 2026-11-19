package no.kristiania.progressservice.integration.security;

import no.kristiania.progressservice.config.JsonAuthenticationEntryPoint;
import no.kristiania.progressservice.config.SecurityConfig;
import no.kristiania.progressservice.progress.controller.ProgressController;
import no.kristiania.progressservice.progress.service.ProgressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static no.kristiania.progressservice.integration.setup.JwtTestSupport.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProgressController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, JsonAuthenticationEntryPoint.class})
class SecurityTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean ProgressService progressService;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.public-key",
                () -> toBase64Pem("PUBLIC KEY", KEYS.getPublic().getEncoded()));
        registry.add("app.jwt.issuer", () -> ISSUER);
    }

    @Test
    void endpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/progress"))
                .andExpect(status().isUnauthorized());
        verify(progressService, never()).findAll(anyLong(), any());
    }

    @Test
    void post_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/progress")
                        .contentType("application/json")
                        .content("{\"goalId\":1,\"date\":\"2026-11-18\"}"))
                .andExpect(status().isUnauthorized());
        verify(progressService, never()).create(anyLong(), any());
    }

    @Test
    void unauthorizedResponse_isJson_withBearerChallenge() throws Exception {
        mockMvc.perform(get("/api/progress"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void garbageToken_returns401() throws Exception {
        getProgress(bearer("not.a.jwt")).andExpect(status().isUnauthorized());
    }

    @Test
    void expiredToken_returns401() throws Exception {
        getProgress(bearer(expiredTokenFor(1L))).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSignedWithOtherKey_returns401() throws Exception {
        getProgress(bearer(tokenSignedWithOtherKey(1L))).andExpect(status().isUnauthorized());
    }

    @Test
    void unsignedToken_returns401() throws Exception {
        getProgress(bearer(unsignedToken(1L))).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWithWrongIssuer_returns401() throws Exception {
        getProgress(bearer(tokenWithIssuer(1L, "someone-else"))).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWithNonNumericSubject_returns401() throws Exception {
        getProgress(bearer(tokenWithSubject("not-a-number"))).andExpect(status().isUnauthorized());
    }

    @Test
    void validToken_isAccepted() throws Exception {
        getProgress(bearer(tokenFor(1L))).andExpect(status().isOk());
    }

    @Test
    void validToken_usesSubjectAsUserId() throws Exception {
        getProgress(bearer(tokenFor(7L))).andExpect(status().isOk());
        verify(progressService).findAll(7L, null);
    }

    private ResultActions getProgress(String authorization) throws Exception {
        return mockMvc.perform(get("/api/progress").header(AUTHORIZATION, authorization));
    }
}
