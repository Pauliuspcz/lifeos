package no.kristiania.lifeos.auth.service;

import no.kristiania.lifeos.auth.dto.AuthResponse;
import no.kristiania.lifeos.auth.dto.LoginRequest;
import no.kristiania.lifeos.auth.dto.RefreshRequest;
import no.kristiania.lifeos.auth.dto.RegisterRequest;
import no.kristiania.lifeos.auth.dto.RegisterResponse;
import no.kristiania.lifeos.auth.token.RefreshToken;
import no.kristiania.lifeos.auth.token.RefreshTokenService;
import no.kristiania.lifeos.exception.EmailAlreadyRegisteredException;
import no.kristiania.lifeos.exception.InvalidCredentialsException;
import no.kristiania.lifeos.exception.InvalidRefreshTokenException;
import no.kristiania.lifeos.user.User;
import no.kristiania.lifeos.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final AuthResponse TOKENS = new AuthResponse("access", "refresh", "Bearer", 3600L);

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock RefreshTokenService refreshTokenService;
    @Mock TokenIssuer tokenIssuer;
    @InjectMocks AuthService service;

    private User user(Long id, String passwordHash) {
        User user = new User();
        user.setId(id);
        user.setEmail("kari@example.com");
        user.setName("Kari");
        user.setPasswordHash(passwordHash);
        return user;
    }

    @Test
    void register_normalizesEmail_trimsName_andStoresEncodedPassword() {
        when(userRepository.existsByEmail("kari@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        RegisterResponse response = service.register(
                new RegisterRequest("  KARI@Example.com ", "password123", "  Kari  "));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("kari@example.com");
        assertThat(saved.getValue().getName()).isEqualTo("Kari");
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("encoded");
        assertThat(response).isEqualTo(new RegisterResponse(1L, "kari@example.com", "Kari"));
    }

    @Test
    void register_duplicateEmail_throws_andSavesNothing() {
        when(userRepository.existsByEmail("kari@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterRequest("kari@example.com", "password123", "Kari")))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_withCorrectPassword_issuesTokens_andNormalizesEmail() {
        User user = user(1L, "hash");
        when(userRepository.findByEmail("kari@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);
        when(tokenIssuer.issueFor(user)).thenReturn(TOKENS);

        AuthResponse response = service.login(new LoginRequest("  KARI@example.com", "password123"));

        assertThat(response).isSameAs(TOKENS);
    }

    @Test
    void login_unknownEmail_throws_withoutCheckingPassword() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("nobody@example.com", "password123")))
                .isInstanceOf(InvalidCredentialsException.class);
        verifyNoInteractions(passwordEncoder, tokenIssuer);
    }

    @Test
    void login_googleOnlyUserWithoutPassword_throws() {
        when(userRepository.findByEmail("kari@example.com")).thenReturn(Optional.of(user(1L, null)));

        assertThatThrownBy(() -> service.login(new LoginRequest("kari@example.com", "password123")))
                .isInstanceOf(InvalidCredentialsException.class);
        verifyNoInteractions(passwordEncoder, tokenIssuer);
    }

    @Test
    void login_wrongPassword_throws_withoutIssuingTokens() {
        when(userRepository.findByEmail("kari@example.com")).thenReturn(Optional.of(user(1L, "hash")));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("kari@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
        verifyNoInteractions(tokenIssuer);
    }

    @Test
    void refresh_withValidToken_issuesNewPairForTheTokenOwner() {
        RefreshToken stored = new RefreshToken();
        stored.setUserId(1L);
        User user = user(1L, "hash");
        when(refreshTokenService.consume("raw")).thenReturn(stored);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tokenIssuer.issueFor(user)).thenReturn(TOKENS);

        assertThat(service.refresh(new RefreshRequest("raw"))).isSameAs(TOKENS);
    }

    @Test
    void refresh_whenUserWasDeleted_throwsInvalidRefreshToken() {
        RefreshToken stored = new RefreshToken();
        stored.setUserId(1L);
        when(refreshTokenService.consume("raw")).thenReturn(stored);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh(new RefreshRequest("raw")))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verifyNoInteractions(tokenIssuer);
    }

    @Test
    void refresh_whenTokenIsInvalid_propagatesTheException() {
        when(refreshTokenService.consume("raw")).thenThrow(new InvalidRefreshTokenException());

        assertThatThrownBy(() -> service.refresh(new RefreshRequest("raw")))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verifyNoInteractions(userRepository, tokenIssuer);
    }

    @Test
    void logout_revokesTheGivenToken() {
        service.logout(new RefreshRequest("raw"));

        verify(refreshTokenService).revoke("raw");
    }
}
