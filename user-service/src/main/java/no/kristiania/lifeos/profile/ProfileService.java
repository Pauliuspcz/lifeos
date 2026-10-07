package no.kristiania.lifeos.profile;

import lombok.RequiredArgsConstructor;
import no.kristiania.lifeos.profile.DTOs.ProfileResponse;
import no.kristiania.lifeos.profile.DTOs.UpdateProfileRequest;
import no.kristiania.lifeos.user.User;
import no.kristiania.lifeos.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;


    @Transactional
    public ProfileResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User user = findUser(userId);

        if (req.name() != null) {
            user.setName(req.name().trim());
        }
        if (req.profilePictureUrl() != null) {
            String url = req.profilePictureUrl().trim();
            user.setProfilePictureUrl(url.isEmpty() ? null : url);
        }
        return toResponse(user);
    }

    public ProfileResponse getProfile(Long userId) {
        return toResponse(findUser(userId));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(user.getId(), user.getEmail(), user.getName(), user.getProfilePictureUrl());
    }
}