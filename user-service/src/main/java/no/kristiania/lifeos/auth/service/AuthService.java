package no.kristiania.lifeos.auth.service;


import lombok.RequiredArgsConstructor;
import no.kristiania.lifeos.auth.dto.*;
import no.kristiania.lifeos.auth.token.RefreshToken;
import no.kristiania.lifeos.auth.token.RefreshTokenService;
import no.kristiania.lifeos.user.Emails;
import no.kristiania.lifeos.user.User;
import no.kristiania.lifeos.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenService refreshTokenService;

    private final TokenIssuer tokenIssuer;

    @Transactional
    public RegisterResponse register(RegisterRequest req) {
        String email = Emails.normalize(req.email());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setName(req.name().trim());
        User saved = userRepository.save(user);
        return new RegisterResponse(saved.getId(), saved.getEmail(), saved.getName());
    }

    public AuthResponse login(LoginRequest req) {
        String email = Emails.normalize(req.email());
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getPasswordHash() != null
                        && passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return tokenIssuer.issueFor(user);
    }

    public AuthResponse refresh(RefreshRequest req) {
        RefreshToken stored = refreshTokenService.consume(req.refreshToken());
        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        return tokenIssuer.issueFor(user);
    }

    public void logout(RefreshRequest req) {
        refreshTokenService.revoke(req.refreshToken());
    }
}
