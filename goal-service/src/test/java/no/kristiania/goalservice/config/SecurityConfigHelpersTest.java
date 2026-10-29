package no.kristiania.goalservice.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityConfigHelpersTest {

    private static KeyPair rsaKeys;
    private static KeyPair ecKeys;

    @BeforeAll
    static void generateKeys() throws Exception {
        KeyPairGenerator rsa = KeyPairGenerator.getInstance("RSA");
        rsa.initialize(2048);
        rsaKeys = rsa.generateKeyPair();
        KeyPairGenerator ec = KeyPairGenerator.getInstance("EC");
        ec.initialize(256);
        ecKeys = ec.generateKeyPair();
    }

    private static String base64Pem(PublicKey key) {
        String body = Base64.getMimeEncoder(64, "\n".getBytes(UTF_8)).encodeToString(key.getEncoded());
        String pem = "-----BEGIN PUBLIC KEY-----\n" + body + "\n-----END PUBLIC KEY-----\n";
        return Base64.getEncoder().encodeToString(pem.getBytes(UTF_8));
    }

    private static Jwt jwtWithSubject(String subject) {
        return Jwt.withTokenValue("token").header("alg", "RS256").claim("iss", "test").subject(subject).build();
    }

    @Test
    void toAuthentication_numericSubject_usesItAsLongPrincipal() {
        Jwt jwt = jwtWithSubject("7");

        AbstractAuthenticationToken token = SecurityConfig.toAuthentication(jwt);

        assertThat(token.getPrincipal()).isEqualTo(7L);
        assertThat(token.getCredentials()).isSameAs(jwt);
        assertThat(token.getAuthorities()).isEmpty();
    }

    @Test
    void toAuthentication_nonNumericSubject_throwsInvalidBearerToken() {
        assertThatThrownBy(() -> SecurityConfig.toAuthentication(jwtWithSubject("not-a-number")))
                .isInstanceOf(InvalidBearerTokenException.class);
    }

    @Test
    void toAuthentication_missingSubject_throwsInvalidBearerToken() {
        Jwt withoutSubject = Jwt.withTokenValue("token").header("alg", "RS256").claim("iss", "test").build();

        assertThatThrownBy(() -> SecurityConfig.toAuthentication(withoutSubject))
                .isInstanceOf(InvalidBearerTokenException.class);
    }

    @Test
    void parsePublicKey_validBase64Pem_returnsTheSameKey() {
        var parsed = SecurityConfig.parsePublicKey(base64Pem(rsaKeys.getPublic()));

        assertThat(parsed.getEncoded()).isEqualTo(rsaKeys.getPublic().getEncoded());
    }

    @Test
    void parsePublicKey_surroundingWhitespace_isIgnored() {
        var parsed = SecurityConfig.parsePublicKey("  " + base64Pem(rsaKeys.getPublic()) + "\n");

        assertThat(parsed.getEncoded()).isEqualTo(rsaKeys.getPublic().getEncoded());
    }

    @Test
    void parsePublicKey_notBase64_throwsWithHelpfulMessage() {
        assertThatThrownBy(() -> SecurityConfig.parsePublicKey("%%% not base64 %%%"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.jwt.public-key");
    }

    @Test
    void parsePublicKey_base64ButNotAKey_throwsWithHelpfulMessage() {
        String garbagePem = "-----BEGIN PUBLIC KEY-----\nAAAA\n-----END PUBLIC KEY-----";
        String value = Base64.getEncoder().encodeToString(garbagePem.getBytes(UTF_8));

        assertThatThrownBy(() -> SecurityConfig.parsePublicKey(value))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.jwt.public-key");
    }

    @Test
    void parsePublicKey_ecKeyInsteadOfRsa_throwsWithHelpfulMessage() {
        assertThatThrownBy(() -> SecurityConfig.parsePublicKey(base64Pem(ecKeys.getPublic())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.jwt.public-key");
    }
}
