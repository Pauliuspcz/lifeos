package no.kristiania.lifeos.profile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.kristiania.lifeos.profile.service.ProfileService;
import no.kristiania.lifeos.profile.dto.ProfileResponse;
import no.kristiania.lifeos.profile.dto.UpdateProfileRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ProfileResponse me(@AuthenticationPrincipal Long userId) {
        return profileService.getProfile(userId);
    }
    @PatchMapping("/me")
    public ProfileResponse update(@AuthenticationPrincipal Long userId,
                                  @Valid @RequestBody UpdateProfileRequest req) {
        return profileService.updateProfile(userId, req);
    }
}
