package no.kristiania.goalservice.unit.config;

import com.jayway.jsonpath.JsonPath;
import no.kristiania.goalservice.config.JsonAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class JsonAuthenticationEntryPointTest {

    private final JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();

    private MockHttpServletResponse commence() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        entryPoint.commence(new MockHttpServletRequest(), response, new BadCredentialsException("secret detail"));
        return response;
    }

    @Test
    void commence_setsStatus401_andBearerChallenge() throws Exception {
        MockHttpServletResponse response = commence();

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Bearer");
    }

    @Test
    void commence_writesJsonBody_withFixedMessage() throws Exception {
        MockHttpServletResponse response = commence();
        String body = response.getContentAsString();

        assertThat(response.getContentType()).startsWith("application/json");
        assertThat((Integer) JsonPath.read(body, "$.status")).isEqualTo(401);
        assertThat((String) JsonPath.read(body, "$.error")).isEqualTo("Unauthorized");
        assertThat((String) JsonPath.read(body, "$.message")).isEqualTo("Authentication required");
    }

    @Test
    void commence_writesParsableTimestamp() throws Exception {
        String body = commence().getContentAsString();

        assertThatCode(() -> Instant.parse(JsonPath.read(body, "$.timestamp"))).doesNotThrowAnyException();
    }

    @Test
    void commence_doesNotLeakExceptionMessage() throws Exception {
        assertThat(commence().getContentAsString()).doesNotContain("secret detail");
    }
}
