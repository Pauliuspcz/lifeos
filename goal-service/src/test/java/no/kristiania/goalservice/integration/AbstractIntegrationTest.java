package no.kristiania.goalservice.integration;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    private static final KeyPair KEY_PAIR = generateKeyPair();
    private static final KeyPair OTHER_KEY_PAIR = generateKeyPair();

    @Autowired protected MockMvc mockMvc;
    @Autowired protected JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void jwtPublicKey(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.public-key",
                () -> toBase64Pem("PUBLIC KEY", KEY_PAIR.getPublic().getEncoded()));
    }

    @BeforeEach
    void cleanDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "goals");
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected static String tokenFor(long userId) {
        return tokenWithSubject(String.valueOf(userId));
    }

    protected static String tokenWithSubject(String subject) {
        Instant now = Instant.now();
        return sign(subject, now, now.plus(1, ChronoUnit.HOURS), KEY_PAIR.getPrivate());
    }

    protected static String expiredTokenFor(long userId) {
        Instant now = Instant.now();
        return sign(String.valueOf(userId), now.minus(2, ChronoUnit.HOURS),
                now.minus(1, ChronoUnit.HOURS), KEY_PAIR.getPrivate());
    }

    protected static String tokenSignedWithOtherKey(long userId) {
        Instant now = Instant.now();
        return sign(String.valueOf(userId), now, now.plus(1, ChronoUnit.HOURS), OTHER_KEY_PAIR.getPrivate());
    }

    protected static String unsignedToken(long userId) {
        return Jwts.builder().subject(String.valueOf(userId)).compact();
    }

    private static String sign(String subject, Instant issuedAt, Instant expiresAt, PrivateKey key) {
        return Jwts.builder()
                .subject(subject)
                .claim("email", "user" + subject + "@example.com")
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String toBase64Pem(String type, byte[] der) {
        String pem = "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8)).encodeToString(der)
                + "\n-----END " + type + "-----\n";
        return Base64.getEncoder().encodeToString(pem.getBytes(StandardCharsets.UTF_8));
    }
}