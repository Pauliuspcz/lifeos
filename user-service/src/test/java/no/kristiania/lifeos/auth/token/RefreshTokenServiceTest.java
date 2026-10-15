package no.kristiania.lifeos.auth.token;

import no.kristiania.lifeos.exception.InvalidRefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock RefreshTokenRepository repository;
    @InjectMocks RefreshTokenService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "refreshDays", 30L);
        ReflectionTestUtils.setField(service, "reuseGraceSeconds", 10L);
    }

    private RefreshToken storedToken(long userId, boolean revoked, Instant expiresAt) {
        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setRevoked(revoked);
        token.setExpiresAt(expiresAt);
        return token;
    }

    private void givenStored(String raw, RefreshToken token) {
        when(repository.findForUpdateByTokenHash(RefreshTokenService.hash(raw)))
                .thenReturn(Optional.ofNullable(token));
    }

    @Test
    void hash_isSha256Hex_andDeterministic() {
        assertThat(RefreshTokenService.hash("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        assertThat(RefreshTokenService.hash("a")).isNotEqualTo(RefreshTokenService.hash("b"));
    }

    @Test
    void create_storesHashNotRawToken_andSetsExpiry() {
        String raw = service.create(7L);

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(saved.capture());
        RefreshToken token = saved.getValue();

        assertThat(raw).isNotBlank();
        assertThat(token.getTokenHash()).isEqualTo(RefreshTokenService.hash(raw)).isNotEqualTo(raw);
        assertThat(token.getUserId()).isEqualTo(7L);
        assertThat(token.getExpiresAt())
                .isBetween(Instant.now().plus(30, ChronoUnit.DAYS).minusSeconds(60),
                        Instant.now().plus(30, ChronoUnit.DAYS).plusSeconds(60));
    }

    @Test
    void create_givesDifferentTokensEachTime() {
        assertThat(service.create(1L)).isNotEqualTo(service.create(1L));
    }

    @Test
    void consume_validToken_marksItRevoked() {
        RefreshToken stored = storedToken(1L, false, Instant.now().plus(1, ChronoUnit.DAYS));
        givenStored("raw", stored);

        RefreshToken result = service.consume("raw");

        assertThat(result.isRevoked()).isTrue();
        verify(repository, never()).revokeAllByUserId(any());
    }

    @Test
    void consume_unknownToken_throws_withoutRevokingAnything() {
        givenStored("raw", null);

        assertThatThrownBy(() -> service.consume("raw"))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verify(repository, never()).revokeAllByUserId(any());
    }

    @Test
    void consume_alreadyRevokedToken_revokesAllTokensForUser_andThrows() {
        givenStored("raw", storedToken(1L, true, Instant.now().plus(1, ChronoUnit.DAYS)));

        assertThatThrownBy(() -> service.consume("raw"))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verify(repository).revokeAllByUserId(1L);
    }

    @Test
    void consume_expiredToken_throws_andIsNotMarkedRevoked() {
        RefreshToken stored = storedToken(1L, false, Instant.now().minus(1, ChronoUnit.DAYS));
        givenStored("raw", stored);

        assertThatThrownBy(() -> service.consume("raw"))
                .isInstanceOf(InvalidRefreshTokenException.class);
        assertThat(stored.isRevoked()).isFalse();
        verify(repository, never()).revokeAllByUserId(any());
    }

    @Test
    void revoke_knownToken_marksItRevoked() {
        RefreshToken stored = storedToken(1L, false, Instant.now().plus(1, ChronoUnit.DAYS));
        when(repository.findByTokenHash(RefreshTokenService.hash("raw"))).thenReturn(Optional.of(stored));

        service.revoke("raw");

        assertThat(stored.isRevoked()).isTrue();
    }

    @Test
    void revoke_unknownToken_doesNothing() {
        when(repository.findByTokenHash(RefreshTokenService.hash("raw"))).thenReturn(Optional.empty());

        service.revoke("raw");

        verify(repository, never()).save(any());
    }

    @Test
    void consume_validToken_setsRevokedAt() {
        RefreshToken stored = storedToken(1L, false, Instant.now().plus(1, ChronoUnit.DAYS));
        givenStored("raw", stored);

        service.consume("raw");

        assertThat(stored.getRevokedAt()).isNotNull();
    }

    @Test
    void consume_tokenRotatedWithinGracePeriod_throws_butDoesNotRevokeAll() {
        RefreshToken stored = storedToken(1L, true, Instant.now().plus(1, ChronoUnit.DAYS));
        stored.setRevokedAt(Instant.now().minusSeconds(2));
        givenStored("raw", stored);

        assertThatThrownBy(() -> service.consume("raw"))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verify(repository, never()).revokeAllByUserId(any());
    }

    @Test
    void consume_tokenRotatedLongAgo_revokesAllTokensForUser() {
        RefreshToken stored = storedToken(1L, true, Instant.now().plus(1, ChronoUnit.DAYS));
        stored.setRevokedAt(Instant.now().minusSeconds(60));
        givenStored("raw", stored);

        assertThatThrownBy(() -> service.consume("raw"))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verify(repository).revokeAllByUserId(1L);
    }
}
