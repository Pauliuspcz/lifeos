package no.kristiania.goalservice.integration.setup;

import io.jsonwebtoken.Jwts;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

public final class JwtTestSupport {

    public static final String ISSUER = "test-issuer";
    public static final KeyPair KEYS = generate();
    public static final KeyPair OTHER_KEYS = generate();

    private JwtTestSupport() {}

    public static String bearer(String token) { return "Bearer " + token; }

    public static String tokenFor(long userId) {
        return tokenWithSubject(String.valueOf(userId));
    }
    public static String tokenWithSubject(String subject) {
        Instant now = Instant.now();
        return build(subject, ISSUER, KEYS.getPrivate(), now, now.plus(1, ChronoUnit.HOURS));
    }
    public static String tokenWithIssuer(long userId, String issuer) {
        Instant now = Instant.now();
        return build(String.valueOf(userId), issuer, KEYS.getPrivate(), now, now.plus(1, ChronoUnit.HOURS));
    }

    public static String tokenSignedWithOtherKey(long userId) {
        Instant now = Instant.now();
        return build(String.valueOf(userId), ISSUER, OTHER_KEYS.getPrivate(), now, now.plus(1, ChronoUnit.HOURS));
    }
    public static String expiredTokenFor(long userId) {
        Instant now = Instant.now();
        return build(String.valueOf(userId), ISSUER, KEYS.getPrivate(),
                now.minus(2, ChronoUnit.HOURS), now.minus(1, ChronoUnit.HOURS));
    }
    public static String unsignedToken(long userId) {
        return Jwts.builder().issuer(ISSUER).subject(String.valueOf(userId))
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS))).compact();
    }

    public static String toBase64Pem(String type, byte[] der) {
        String pem = "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8)).encodeToString(der)
                + "\n-----END " + type + "-----\n";
        return Base64.getEncoder().encodeToString(pem.getBytes(StandardCharsets.UTF_8));
    }

    private static String build(String subject, String issuer, PrivateKey key,
                                Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .issuer(issuer)
                .subject(subject)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    private static KeyPair generate() {
        try {
            KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
            g.initialize(2048);
            return g.generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
