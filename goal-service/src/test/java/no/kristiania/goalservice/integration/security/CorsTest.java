package no.kristiania.goalservice.integration.security;

import no.kristiania.goalservice.integration.setup.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static no.kristiania.goalservice.integration.setup.JwtTestSupport.bearer;
import static no.kristiania.goalservice.integration.setup.JwtTestSupport.tokenFor;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CorsTest extends AbstractIntegrationTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:5173";
    private static final String UNKNOWN_ORIGIN = "http://evil.example";

    @ParameterizedTest(name = "preflight for {0} from allowed origin is accepted without token")
    @ValueSource(strings = {"GET", "POST", "PUT", "PATCH", "DELETE"})
    void preflight_fromAllowedOrigin_isAccepted(String method) throws Exception {
        mockMvc.perform(options("/api/goal")
                        .header(ORIGIN, ALLOWED_ORIGIN)
                        .header(ACCESS_CONTROL_REQUEST_METHOD, method)
                        .header(ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_METHODS, containsString(method)))
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_HEADERS,
                        containsStringIgnoringCase("authorization")))
                .andExpect(header().string(ACCESS_CONTROL_MAX_AGE, "3600"));
    }

    @Test
    void preflight_fromUnknownOrigin_isRejected() throws Exception {
        mockMvc.perform(options("/api/goal")
                        .header(ORIGIN, UNKNOWN_ORIGIN)
                        .header(ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void preflight_withDisallowedMethod_isRejected() throws Exception {
        mockMvc.perform(options("/api/goal")
                        .header(ORIGIN, ALLOWED_ORIGIN)
                        .header(ACCESS_CONTROL_REQUEST_METHOD, "TRACE"))
                .andExpect(status().isForbidden());
    }

    @Test
    void preflight_withDisallowedHeader_isRejected() throws Exception {
        mockMvc.perform(options("/api/goal")
                        .header(ORIGIN, ALLOWED_ORIGIN)
                        .header(ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(ACCESS_CONTROL_REQUEST_HEADERS, "x-custom"))
                .andExpect(status().isForbidden());
    }

    @Test
    void request_fromUnknownOrigin_isRejected_evenWithValidToken() throws Exception {
        mockMvc.perform(get("/api/goal")
                        .header(ORIGIN, UNKNOWN_ORIGIN)
                        .header(AUTHORIZATION, bearer(tokenFor(1L))))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void unauthorizedResponse_stillCarriesCorsHeader() throws Exception {
        mockMvc.perform(get("/api/goal").header(ORIGIN, ALLOWED_ORIGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
    }

    @Test
    void successfulResponse_carriesCorsHeader() throws Exception {
        mockMvc.perform(get("/api/goal")
                        .header(ORIGIN, ALLOWED_ORIGIN)
                        .header(AUTHORIZATION, bearer(tokenFor(1L))))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
    }

    @Test
    void request_withoutOriginHeader_hasNoCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/goal").header(AUTHORIZATION, bearer(tokenFor(1L))))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}