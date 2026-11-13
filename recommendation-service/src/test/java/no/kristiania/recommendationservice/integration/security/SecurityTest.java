package no.kristiania.recommendationservice.integration.security;

import no.kristiania.recommendationservice.config.JsonAuthenticationEntryPoint;
import no.kristiania.recommendationservice.config.SecurityConfig;
import no.kristiania.recommendationservice.weather.WeatherController;
import no.kristiania.recommendationservice.weather.WeatherService;
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

import static no.kristiania.recommendationservice.integration.setup.JwtTestSupport.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, JsonAuthenticationEntryPoint.class})
class SecurityTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean WeatherService weatherService;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.public-key",
                () -> toBase64Pem("PUBLIC KEY", KEYS.getPublic().getEncoded()));
        registry.add("app.jwt.issuer", () -> ISSUER);
    }

    @Test
    void endpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/recommendations/weather").param("lat", "59.91").param("lon", "10.75"))
                .andExpect(status().isUnauthorized());
        verify(weatherService, never()).getRecommendation(anyDouble(), anyDouble());
    }

    @Test
    void unauthorizedResponse_isJson_withBearerChallenge() throws Exception {
        mockMvc.perform(get("/api/recommendations/weather").param("lat", "59.91").param("lon", "10.75"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void garbageToken_returns401() throws Exception {
        getWeather(bearer("not.a.jwt")).andExpect(status().isUnauthorized());
    }

    @Test
    void expiredToken_returns401() throws Exception {
        getWeather(bearer(expiredTokenFor(1L))).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSignedWithOtherKey_returns401() throws Exception {
        getWeather(bearer(tokenSignedWithOtherKey(1L))).andExpect(status().isUnauthorized());
    }

    @Test
    void unsignedToken_returns401() throws Exception {
        getWeather(bearer(unsignedToken(1L))).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWithWrongIssuer_returns401() throws Exception {
        getWeather(bearer(tokenWithIssuer(1L, "someone-else"))).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWithNonNumericSubject_returns401() throws Exception {
        getWeather(bearer(tokenWithSubject("not-a-number"))).andExpect(status().isUnauthorized());
    }

    @Test
    void validToken_isAccepted() throws Exception {
        getWeather(bearer(tokenFor(1L))).andExpect(status().isOk());
    }

    private ResultActions getWeather(String authorization) throws Exception {
        return mockMvc.perform(get("/api/recommendations/weather").param("lat", "59.91").param("lon", "10.75")
                .header(AUTHORIZATION, authorization));
    }
}