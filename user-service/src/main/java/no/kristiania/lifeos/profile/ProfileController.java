package no.kristiania.lifeos.profile;

import lombok.RequiredArgsConstructor;
import no.kristiania.lifeos.profile.DTOs.ProfileResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ProfileResponse me(@AuthenticationPrincipal Long userId) {
        return profileService.getProfile(userId);
    }
}
