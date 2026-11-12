package no.kristiania.recommendationservice.integration.setup;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @RegisterExtension
    static WireMockExtension openMeteo = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void openMeteoProperties(DynamicPropertyRegistry registry) {
        registry.add("app.open-meteo.url", openMeteo::baseUrl);
    }

    @Autowired protected MockMvc mockMvc;

    protected ResultActions getWeather(double lat, double lon) throws Exception {
        return mockMvc.perform(get("/api/recommendations/weather")
                .param("lat", String.valueOf(lat))
                .param("lon", String.valueOf(lon)));
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
}
