package no.kristiania.lifeos.auth.service;

import lombok.RequiredArgsConstructor;
import no.kristiania.lifeos.auth.dto.AuthResponse;
import no.kristiania.lifeos.auth.token.JwtService;
import no.kristiania.lifeos.auth.token.RefreshTokenService;
import no.kristiania.lifeos.user.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenIssuer {
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse issueFor(User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                refreshTokenService.create(user.getId()),
                "Bearer",
                jwtService.accessExpirySeconds());
    }
}
