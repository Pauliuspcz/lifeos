package no.kristiania.goalservice.integration.security;

import no.kristiania.goalservice.integration.setup.AbstractGoalApiTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static no.kristiania.goalservice.integration.setup.JwtTestSupport.bearer;
import static no.kristiania.goalservice.integration.setup.JwtTestSupport.expiredTokenFor;
import static no.kristiania.goalservice.integration.setup.JwtTestSupport.tokenFor;
import static no.kristiania.goalservice.integration.setup.JwtTestSupport.tokenSignedWithOtherKey;
import static no.kristiania.goalservice.integration.setup.JwtTestSupport.tokenWithIssuer;
import static no.kristiania.goalservice.integration.setup.JwtTestSupport.tokenWithSubject;
import static no.kristiania.goalservice.integration.setup.JwtTestSupport.unsignedToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityTest extends AbstractGoalApiTest {

    static Stream<Arguments> allEndpoints() {
        return Stream.of(
                Arguments.of(HttpMethod.POST, "/api/goal"),
                Arguments.of(HttpMethod.GET, "/api/goal"),
                Arguments.of(HttpMethod.GET, "/api/goal/1"),
                Arguments.of(HttpMethod.PUT, "/api/goal/1"),
                Arguments.of(HttpMethod.PATCH, "/api/goal/1/archive"),
                Arguments.of(HttpMethod.PATCH, "/api/goal/1/unarchive"),
                Arguments.of(HttpMethod.DELETE, "/api/goal/1"));
    }

    @ParameterizedTest(name = "{0} {1} without token returns 401")
    @MethodSource("allEndpoints")
    void endpoint_withoutToken_returns401(HttpMethod method, String path) throws Exception {
        mockMvc.perform(request(method, path))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthorizedResponse_isJson_withBearerChallenge() throws Exception {
        mockMvc.perform(get("/api/goal"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void garbageToken_returns401() throws Exception {
        getGoals(bearer("not.a.jwt")).andExpect(status().isUnauthorized());
    }

    @Test
    void expiredToken_returns401() throws Exception {
        getGoals(bearer(expiredTokenFor(1L))).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSignedWithOtherKey_returns401() throws Exception {
        getGoals(bearer(tokenSignedWithOtherKey(1L))).andExpect(status().isUnauthorized());
    }

    @Test
    void unsignedToken_returns401() throws Exception {
        getGoals(bearer(unsignedToken(1L))).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWithWrongIssuer_returns401() throws Exception {
        getGoals(bearer(tokenWithIssuer(1L, "someone-else"))).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWithNonNumericSubject_returns401() throws Exception {
        getGoals(bearer(tokenWithSubject("not-a-number"))).andExpect(status().isUnauthorized());
    }

    @Test
    void validToken_isAccepted() throws Exception {
        getGoals(bearer(tokenFor(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void validToken_usesSubjectAsUserId() throws Exception {
        // Given a token whose subject is 7, When a goal is created with it
        createGoal(7L, VALID_GOAL).andExpect(status().isCreated());

        // Then the goal is stored for user 7
        Long storedUserId = jdbcTemplate.queryForObject("select user_id from goals", Long.class);
        assertThat(storedUserId).isEqualTo(7L);
    }

    private ResultActions getGoals(String authorization) throws Exception {
        return mockMvc.perform(get("/api/goal").header(AUTHORIZATION, authorization));
    }
}