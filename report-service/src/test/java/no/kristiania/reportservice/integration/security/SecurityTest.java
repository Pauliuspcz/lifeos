package no.kristiania.reportservice.integration.security;

import no.kristiania.reportservice.config.JsonAuthenticationEntryPoint;
import no.kristiania.reportservice.config.SecurityConfig;
import no.kristiania.reportservice.report.ReportController;
import no.kristiania.reportservice.report.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static no.kristiania.reportservice.integration.setup.JwtTestSupport.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportController.class, properties = {
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.config.enabled=false"
})
@Import({SecurityConfig.class, JsonAuthenticationEntryPoint.class})
class SecurityTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean ReportService reportService;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.public-key",
                () -> toBase64Pem("PUBLIC KEY", KEYS.getPublic().getEncoded()));
        registry.add("app.jwt.issuer", () -> ISSUER);
    }

    @Test
    void endpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/reports/goals/1"))
                .andExpect(status().isUnauthorized());
        verify(reportService, never()).getGoalReport(anyLong(), anyLong());
    }

    @Test
    void unauthorizedResponse_isJson_withBearerChallenge() throws Exception {
        mockMvc.perform(get("/api/reports/goals/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void garbageToken_returns401() throws Exception {
        getReport(bearer("not.a.jwt")).andExpect(status().isUnauthorized());
    }

    @Test
    void expiredToken_returns401() throws Exception {
        getReport(bearer(expiredTokenFor(1L))).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSignedWithOtherKey_returns401() throws Exception {
        getReport(bearer(tokenSignedWithOtherKey(1L))).andExpect(status().isUnauthorized());
    }

    @Test
    void unsignedToken_returns401() throws Exception {
        getReport(bearer(unsignedToken(1L))).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWithWrongIssuer_returns401() throws Exception {
        getReport(bearer(tokenWithIssuer(1L, "someone-else"))).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWithNonNumericSubject_returns401() throws Exception {
        getReport(bearer(tokenWithSubject("not-a-number"))).andExpect(status().isUnauthorized());
    }

    @Test
    void validToken_isAccepted() throws Exception {
        getReport(bearer(tokenFor(1L))).andExpect(status().isOk());
    }

    @Test
    void validToken_usesSubjectAsUserId() throws Exception {
        getReport(bearer(tokenFor(7L))).andExpect(status().isOk());
        verify(reportService).getGoalReport(7L, 1L);
    }

    private ResultActions getReport(String authorization) throws Exception {
        return mockMvc.perform(get("/api/reports/goals/1").header(AUTHORIZATION, authorization));
    }
}
