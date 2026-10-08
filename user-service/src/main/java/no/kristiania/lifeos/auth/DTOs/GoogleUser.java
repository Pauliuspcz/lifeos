package no.kristiania.lifeos.auth.DTOs;

public record GoogleUser(String sub, String email, String name, String picture) {}
