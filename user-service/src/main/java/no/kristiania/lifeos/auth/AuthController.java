package no.kristiania.lifeos.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.kristiania.lifeos.auth.DTOs.AuthResponse;
import no.kristiania.lifeos.auth.DTOs.LoginRequest;
import no.kristiania.lifeos.auth.DTOs.RegisterRequest;
import no.kristiania.lifeos.auth.DTOs.RegisterResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
