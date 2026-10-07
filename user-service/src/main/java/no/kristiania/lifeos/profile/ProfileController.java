package no.kristiania.lifeos.profile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.kristiania.lifeos.profile.DTOs.ProfileResponse;
import no.kristiania.lifeos.profile.DTOs.UpdateProfileRequest;
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
