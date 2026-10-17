package no.kristiania.lifeos.auth.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import no.kristiania.lifeos.user.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static KeyPair keyPair;
    private static KeyPair otherKeyPair;

    @BeforeAll
    static void generateKeys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
        otherKeyPair = generator.generateKeyPair();
    }

    private static String toBase64Pem(String type, byte[] der) {
        String pem = "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8)).encodeToString(der)
                + "\n-----END " + type + "-----\n";
        return Base64.getEncoder().encodeToString(pem.getBytes(StandardCharsets.UTF_8));
    }

    private static JwtService serviceWith(KeyPair pair, long accessMinutes) throws Exception {
        return new JwtService(
                toBase64Pem("PRIVATE KEY", pair.getPrivate().getEncoded()),
                toBase64Pem("PUBLIC KEY", pair.getPublic().getEncoded()),
                accessMinutes);
    }

    private static User user(long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName("Kari");
        return user;
    }

    @Test
    void generatedToken_canBeParsed_andContainsSubjectAndEmail() throws Exception {
        JwtService service = serviceWith(keyPair, 60);

        Claims claims = service.parse(service.generateAccessToken(user(42L, "kari@example.com")));

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("email", String.class)).isEqualTo("kari@example.com");
    }

    @Test
    void generatedToken_expiresAfterConfiguredMinutes() throws Exception {
        JwtService service = serviceWith(keyPair, 60);

        Claims claims = service.parse(service.generateAccessToken(user(1L, "a@example.com")));

        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime())
                .isEqualTo(60 * 60 * 1000L);
    }

    @Test
    void accessExpirySeconds_isMinutesTimes60() throws Exception {
        assertThat(serviceWith(keyPair, 60).accessExpirySeconds()).isEqualTo(3600);
        assertThat(serviceWith(keyPair, 5).accessExpirySeconds()).isEqualTo(300);
    }

    @Test
    void generatedToken_isSignedWithRs256() throws Exception {
        JwtService service = serviceWith(keyPair, 60);

        String token = service.generateAccessToken(user(1L, "a@example.com"));
        String header = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]), StandardCharsets.UTF_8);

        assertThat(header).contains("\"alg\":\"RS256\"");
    }

    @Test
    void parse_expiredToken_throws() throws Exception {
        JwtService service = serviceWith(keyPair, -1);

        String token = service.generateAccessToken(user(1L, "a@example.com"));

        assertThatThrownBy(() -> service.parse(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void parse_tokenSignedWithOtherKey_throws() throws Exception {
        JwtService service = serviceWith(keyPair, 60);
        String foreignToken = serviceWith(otherKeyPair, 60).generateAccessToken(user(1L, "a@example.com"));

        assertThatThrownBy(() -> service.parse(foreignToken)).isInstanceOf(JwtException.class);
    }

    @Test
    void parse_tokenWithSwappedPayload_throws() throws Exception {
        JwtService service = serviceWith(keyPair, 60);
        String[] kari = service.generateAccessToken(user(1L, "kari@example.com")).split("\\.");
        String[] admin = service.generateAccessToken(user(2L, "admin@example.com")).split("\\.");
        String forged = kari[0] + "." + admin[1] + "." + kari[2];

        assertThatThrownBy(() -> service.parse(forged)).isInstanceOf(JwtException.class);
    }

    @Test
    void parse_unsignedToken_throws() throws Exception {
        JwtService service = serviceWith(keyPair, 60);
        String unsigned = Jwts.builder().subject("42").compact();

        assertThatThrownBy(() -> service.parse(unsigned)).isInstanceOf(JwtException.class);
    }

    @Test
    void parse_garbage_throws() throws Exception {
        JwtService service = serviceWith(keyPair, 60);

        assertThatThrownBy(() -> service.parse("not.a.jwt")).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> service.parse("")).isInstanceOf(IllegalArgumentException.class);
    }
}