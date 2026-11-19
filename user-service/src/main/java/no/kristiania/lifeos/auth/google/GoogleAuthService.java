package no.kristiania.lifeos.auth.google;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.kristiania.lifeos.auth.dto.AuthResponse;
import no.kristiania.lifeos.auth.dto.GoogleLoginRequest;
import no.kristiania.lifeos.auth.service.TokenIssuer;
import no.kristiania.lifeos.exception.GoogleAccountConflictException;
import no.kristiania.lifeos.user.Emails;
import no.kristiania.lifeos.user.User;
import no.kristiania.lifeos.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final TokenIssuer tokenIssuer;

    @Transactional
    public AuthResponse login(GoogleLoginRequest req) {
        GoogleUser google = googleTokenVerifier.verify(req.idToken());
        User user = userRepository.findByGoogleId(google.sub())
                .orElseGet(() -> linkOrCreate(google));
        log.info("Google login userId={}", user.getId());
        return tokenIssuer.issueFor(user);
    }

    private User linkOrCreate(GoogleUser google) {
        String email = Emails.normalize(google.email());
        return userRepository.findByEmail(email)
                .map(existing -> linkExisting(existing, google))
                .orElseGet(() -> createNew(email, google));
    }

    private User linkExisting(User existing, GoogleUser google) {
        if (existing.getGoogleId() != null && !existing.getGoogleId().equals(google.sub())) {
            log.warn("Google account conflict userId={}", existing.getId());
            throw new GoogleAccountConflictException();
        }
        existing.setGoogleId(google.sub());
        if (existing.getProfilePictureUrl() == null) {
            existing.setProfilePictureUrl(google.picture());
        }
        User saved = userRepository.save(existing);
        log.info("Google account linked userId={}", saved.getId());
        return saved;
    }

    private User createNew(String email, GoogleUser google) {
        User user = new User();
        user.setEmail(email);
        user.setGoogleId(google.sub());
        user.setName(google.name() != null ? google.name() : email.substring(0, email.indexOf('@')));
        user.setProfilePictureUrl(google.picture());
        User saved = userRepository.save(user);
        log.info("User created via Google userId={}", saved.getId());
        return saved;
    }
}