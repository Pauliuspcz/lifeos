package no.kristiania.lifeos.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import no.kristiania.lifeos.auth.DTOs.AuthResponse;
import no.kristiania.lifeos.auth.DTOs.LoginRequest;
import no.kristiania.lifeos.auth.DTOs.RegisterRequest;
import no.kristiania.lifeos.auth.DTOs.RegisterResponse;
import no.kristiania.lifeos.user.User;
import no.kristiania.lifeos.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public RegisterResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
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
        String email = req.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getPasswordHash() != null
                        && passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                refreshTokenService.create(user.getId()),
                "Bearer",
                jwtService.accessExpirySeconds());
    }
}
