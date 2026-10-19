package no.kristiania.goalservice.integration;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CorsTest extends AbstractIntegrationTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:5173";

    @Test
    void preflight_fromAllowedOrigin_isAccepted_withoutToken() throws Exception {
        mockMvc.perform(options("/api/goal")
                        .header(ORIGIN, ALLOWED_ORIGIN)
                        .header(ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_METHODS, containsString("POST")));
    }

    @Test
    void preflight_fromUnknownOrigin_isRejected() throws Exception {
        mockMvc.perform(options("/api/goal")
                        .header(ORIGIN, "http://evil.example")
                        .header(ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthorizedResponse_stillCarriesCorsHeader() throws Exception {
        mockMvc.perform(get("/api/goal").header(ORIGIN, ALLOWED_ORIGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
    }

    @Test
    void authenticatedRequest_fromAllowedOrigin_carriesCorsHeader() throws Exception {
        mockMvc.perform(get("/api/goal")
                        .header(ORIGIN, ALLOWED_ORIGIN)
                        .header(AUTHORIZATION, bearer(tokenFor(1L))))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
    }
}
