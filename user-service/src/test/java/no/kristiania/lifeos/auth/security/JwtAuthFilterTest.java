package no.kristiania.lifeos.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import no.kristiania.lifeos.auth.token.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock JwtService jwtService;

    private JwtAuthFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthFilter(jwtService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Claims claimsWithSubject(String subject) {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(subject);
        return claims;
    }

    @Test
    void validBearerToken_setsAuthenticationWithUserIdAsPrincipal() throws Exception {
        request.addHeader("Authorization", "Bearer valid-token");
        Claims claims = claimsWithSubject("42");
        when(jwtService.parse("valid-token")).thenReturn(claims);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(42L);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void noAuthorizationHeader_leavesRequestUnauthenticated_andContinuesChain() throws Exception {
        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(jwtService);
    }

    @Test
    void nonBearerHeader_isIgnored() throws Exception {
        request.addHeader("Authorization", "Basic abc123");

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(jwtService);
    }

    @Test
    void invalidToken_leavesRequestUnauthenticated_andContinuesChain() throws Exception {
        request.addHeader("Authorization", "Bearer bad-token");
        when(jwtService.parse("bad-token")).thenThrow(new JwtException("invalid"));

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void invalidToken_clearsAnyExistingAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(7L, null, java.util.List.of()));
        request.addHeader("Authorization", "Bearer bad-token");
        when(jwtService.parse("bad-token")).thenThrow(new JwtException("invalid"));

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void tokenWithNonNumericSubject_isRejected() throws Exception {
        request.addHeader("Authorization", "Bearer odd-token");
        Claims claims = claimsWithSubject("not-a-number");
        when(jwtService.parse("odd-token")).thenReturn(claims);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }
}
