package no.kristiania.lifeos.auth.DTOs;

public record AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {}