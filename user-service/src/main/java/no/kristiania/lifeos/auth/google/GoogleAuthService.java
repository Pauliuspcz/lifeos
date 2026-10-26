package no.kristiania.lifeos.auth.google;

import lombok.RequiredArgsConstructor;
import no.kristiania.lifeos.auth.dto.AuthResponse;
import no.kristiania.lifeos.auth.dto.GoogleLoginRequest;
import no.kristiania.lifeos.auth.service.TokenIssuer;
import no.kristiania.lifeos.exception.GoogleAccountConflictException;
import no.kristiania.lifeos.user.Emails;
import no.kristiania.lifeos.user.User;
import no.kristiania.lifeos.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            throw new GoogleAccountConflictException();
        }
        existing.setGoogleId(google.sub());
        if (existing.getProfilePictureUrl() == null) {
            existing.setProfilePictureUrl(google.picture());
        }
        return userRepository.save(existing);
    }

    private User createNew(String email, GoogleUser google) {
        User user = new User();
        user.setEmail(email);
        user.setGoogleId(google.sub());
        user.setName(google.name() != null ? google.name() : email.substring(0, email.indexOf('@')));
        user.setProfilePictureUrl(google.picture());
        return userRepository.save(user);
    }
}
