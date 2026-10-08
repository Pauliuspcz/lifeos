package no.kristiania.lifeos.auth;


import lombok.RequiredArgsConstructor;
import no.kristiania.lifeos.auth.DTOs.*;
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

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    private final GoogleTokenVerifier googleTokenVerifier;

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
    private AuthResponse issueTokens(User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                refreshTokenService.create(user.getId()),
                "Bearer",
                jwtService.accessExpirySeconds());
    }
    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getPasswordHash() != null
                        && passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return issueTokens(user);
    }

    public AuthResponse refresh(RefreshRequest req) {
        RefreshToken stored = refreshTokenService.consume(req.refreshToken());
        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        return issueTokens(user);
    }

    public AuthResponse googleLogin(GoogleLoginRequest req) {
        GoogleUser g = googleTokenVerifier.verify(req.idToken());
        User user = userRepository.findByGoogleId(g.sub())
                .orElseGet(() -> linkOrCreate(g));
        return issueTokens(user);
    }

    private User linkOrCreate(GoogleUser g) {
        String email = g.email().trim().toLowerCase();
        return userRepository.findByEmail(email)
                .map(existing -> {
                    if (existing.getGoogleId() != null && !existing.getGoogleId().equals(g.sub())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Account linked to another Google user");
                    }
                    existing.setGoogleId(g.sub());
                    if (existing.getProfilePictureUrl() == null) {
                        existing.setProfilePictureUrl(g.picture());
                    }
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(email);
                    u.setGoogleId(g.sub());
                    u.setName(g.name() != null ? g.name() : email.substring(0, email.indexOf('@')));
                    u.setProfilePictureUrl(g.picture());
                    return userRepository.save(u);
                });
    }

    public void logout(RefreshRequest req) {
        refreshTokenService.revoke(req.refreshToken());
    }
}
