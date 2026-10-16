package no.kristiania.lifeos.auth.token;

import lombok.RequiredArgsConstructor;
import no.kristiania.lifeos.exception.InvalidRefreshTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.jwt.refresh-expiration-days}")
    private long refreshDays;

    @Value("${app.jwt.refresh-reuse-grace-seconds:10}")
    private long reuseGraceSeconds;

    @Transactional
    public String create(Long userId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setTokenHash(hash(raw));
        token.setExpiresAt(Instant.now().plus(refreshDays, ChronoUnit.DAYS));
        repository.save(token);
        return raw;
    }

    static String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public RefreshToken consume(String raw) {
        RefreshToken token = repository.findForUpdateByTokenHash(hash(raw))
                .orElseThrow(RefreshTokenService::invalid);

        if (token.isRevoked()) {
            if (!rotatedRecently(token)) {
                repository.revokeAllByUserId(token.getUserId());
            }
            throw invalid();
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw invalid();
        }
        token.setRevoked(true);
        token.setRevokedAt(Instant.now());
        return token;
    }

    private boolean rotatedRecently(RefreshToken token) {
        Instant revokedAt = token.getRevokedAt();
        return revokedAt != null
                && revokedAt.plusSeconds(reuseGraceSeconds).isAfter(Instant.now());
    }

    private static InvalidRefreshTokenException invalid() {
        return new InvalidRefreshTokenException();
    }

    @Transactional
    public void revoke(String raw) {
        repository.findByTokenHash(hash(raw)).ifPresent(t -> t.setRevoked(true));
    }
}