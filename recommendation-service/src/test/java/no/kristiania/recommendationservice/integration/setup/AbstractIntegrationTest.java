package no.kristiania.recommendationservice.integration.setup;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import no.kristiania.recommendationservice.weather.CachedWeather;
import no.kristiania.recommendationservice.weather.Location;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static no.kristiania.recommendationservice.integration.setup.JwtTestSupport.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    static WireMockServer openMeteo = new WireMockServer(wireMockConfig().dynamicPort());

    static {
        openMeteo.start();
    }

    @DynamicPropertySource
    static void openMeteoProperties(DynamicPropertyRegistry registry) {
        registry.add("app.open-meteo.url", openMeteo::baseUrl);
    }

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.public-key",
                () -> toBase64Pem("PUBLIC KEY", KEYS.getPublic().getEncoded()));
        registry.add("app.jwt.issuer", () -> ISSUER);
    }

    @Autowired protected MockMvc mockMvc;

    @Autowired protected Cache<Location, CachedWeather> weatherCache;

    @Autowired protected JdbcTemplate jdbcTemplate;

    @Autowired protected RabbitTemplate rabbitTemplate;

    @BeforeEach
    protected void reset() {
        openMeteo.resetAll();
        weatherCache.invalidateAll();
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "outdoor_goal");
    }

    protected ResultActions getWeather(double lat, double lon) throws Exception {
        return mockMvc.perform(get("/api/recommendations/weather")
                .param("lat", String.valueOf(lat))
                .param("lon", String.valueOf(lon))
                .header(AUTHORIZATION, bearer(tokenFor(1L))));
    }

    protected ResultActions postComplements(String json) throws Exception {
        return mockMvc.perform(post("/api/recommendations/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(AUTHORIZATION, bearer(tokenFor(1L))));
    }

    protected ResultActions postComplementsWithoutToken(String json) throws Exception {
        return mockMvc.perform(post("/api/recommendations/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    protected void openMeteoReturns(String json) {
        openMeteo.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/forecast"))
                .willReturn(WireMock.okJson(json)));
    }

    protected void openMeteoFails() {
        openMeteo.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/forecast"))
                .willReturn(WireMock.serverError()));
    }

    protected void openMeteoRespondsAfter(int millis, String json) {
        openMeteo.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/forecast"))
                .willReturn(WireMock.okJson(json).withFixedDelay(millis)));
    }
    protected void verifyOpenMeteoCalled(int times) {
        openMeteo.verify(times, WireMock.getRequestedFor(WireMock.urlPathEqualTo("/v1/forecast")));
    }
    protected void makeCachedWeatherOlder(double lat, double lon, Duration age) {
        Location location = Location.rounded(lat, lon);
        CachedWeather cached = weatherCache.getIfPresent(location);
        weatherCache.put(location, new CachedWeather(cached.weather(), cached.fetchedAt().minus(age)));
    }
}
