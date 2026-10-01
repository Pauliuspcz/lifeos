package no.kristiania.lifeos.profile;

import lombok.RequiredArgsConstructor;
import no.kristiania.lifeos.profile.DTOs.ProfileResponse;
import no.kristiania.lifeos.user.User;
import no.kristiania.lifeos.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new ProfileResponse(user.getId(), user.getEmail(), user.getName(), user.getProfilePictureUrl());
    }
}