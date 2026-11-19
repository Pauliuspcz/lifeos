package no.kristiania.lifeos.profile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.lifeos.exception.NotFoundException;
import no.kristiania.lifeos.profile.dto.ProfileResponse;
import no.kristiania.lifeos.profile.dto.UpdateProfileRequest;
import no.kristiania.lifeos.user.User;
import no.kristiania.lifeos.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
        log.info("Profile updated userId={}", userId);
        return toResponse(user);
    }

    public ProfileResponse getProfile(Long userId) {
        return toResponse(findUser(userId));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(user.getId(), user.getEmail(), user.getName(), user.getProfilePictureUrl());
    }
}