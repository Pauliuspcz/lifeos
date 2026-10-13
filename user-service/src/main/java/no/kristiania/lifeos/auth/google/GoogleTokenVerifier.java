package no.kristiania.lifeos.auth.google;

import no.kristiania.lifeos.exception.InvalidGoogleTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;

import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class GoogleTokenVerifier {

    private static final String JWK_URI = "https://www.googleapis.com/oauth2/v3/certs";

    private final NimbusJwtDecoder decoder;

    public GoogleTokenVerifier(@Value("${app.google.client-id}") String clientId) {
        NimbusJwtDecoder d = NimbusJwtDecoder.withJwkSetUri(JWK_URI).build();

        OAuth2TokenValidator<Jwt> issuerAndAudience = jwt -> {
            String iss = jwt.getClaimAsString("iss");
            boolean ok = ("accounts.google.com".equals(iss) || "https://accounts.google.com".equals(iss))
                    && jwt.getAudience() != null
                    && jwt.getAudience().contains(clientId);
            return ok
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid issuer or audience", null));
        };
        d.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(), issuerAndAudience));
        this.decoder = d;
    }

    public GoogleUser verify(String idToken) {
        try {
            Jwt jwt = decoder.decode(idToken);
            if (!Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"))) {
                throw new InvalidGoogleTokenException();
            }
            return new GoogleUser(jwt.getSubject(), jwt.getClaimAsString("email"),
                    jwt.getClaimAsString("name"), jwt.getClaimAsString("picture"));
        } catch (JwtException e) {
            throw new InvalidGoogleTokenException();
        }
    }
}
