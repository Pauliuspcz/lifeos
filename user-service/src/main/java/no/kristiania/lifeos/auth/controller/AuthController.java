package no.kristiania.lifeos.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.kristiania.lifeos.auth.dto.*;
import no.kristiania.lifeos.auth.google.GoogleAuthService;
import no.kristiania.lifeos.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req);
    }

    @PostMapping("/google")
    public AuthResponse google(@Valid @RequestBody GoogleLoginRequest req) {
        return googleAuthService.login(req);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req);
    }
}
