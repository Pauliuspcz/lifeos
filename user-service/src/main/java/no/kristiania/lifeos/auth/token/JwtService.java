package no.kristiania.lifeos.auth.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import no.kristiania.lifeos.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Service
public class JwtService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long accessMinutes;
    private final String issuer;

    public JwtService(@Value("${app.jwt.private-key}") String privateKeyB64,
                      @Value("${app.jwt.public-key}") String publicKeyB64,
                      @Value("${app.jwt.access-expiration-minutes}") long accessMinutes,
                      @Value("${app.jwt.issuer}") String issuer) throws Exception {
        this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(
                new PKCS8EncodedKeySpec(pemToBytes(decode(privateKeyB64))));
        this.publicKey = KeyFactory.getInstance("RSA").generatePublic(
                new X509EncodedKeySpec(pemToBytes(decode(publicKeyB64))));
        this.accessMinutes = accessMinutes;
        this.issuer = issuer;
    }

    private static String decode(String b64) {
        return new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessMinutes, ChronoUnit.MINUTES)))
                .signWith(privateKey)
                .compact();
    }

    public long accessExpirySeconds() {
        return accessMinutes * 60;
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(publicKey).build()
                .parseSignedClaims(token).getPayload();
    }

    private static byte[] pemToBytes(String pem) {
        String base64 = pem
                .replaceAll("-----BEGIN [A-Z ]+-----", "")
                .replaceAll("-----END [A-Z ]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}