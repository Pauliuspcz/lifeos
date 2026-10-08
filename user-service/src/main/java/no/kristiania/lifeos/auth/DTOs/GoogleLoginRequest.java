package no.kristiania.lifeos.auth.DTOs;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(@NotBlank String idToken) {}
