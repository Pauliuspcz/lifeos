package no.kristiania.lifeos.profile.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Pattern(regexp = ".*\\S.*", message = "must not be blank")
        @Size(max = 255)
        String name,

        @Size(max = 500)
        String profilePictureUrl) {}