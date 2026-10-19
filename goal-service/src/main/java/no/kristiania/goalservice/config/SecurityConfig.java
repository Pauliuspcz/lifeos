package no.kristiania.goalservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JsonAuthenticationEntryPoint entryPoint) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(entryPoint)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(this::toAuthentication)))
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${app.jwt.public-key}") String publicKeyBase64Pem) {
        return NimbusJwtDecoder.withPublicKey(parsePublicKey(publicKeyBase64Pem)).build();
    }

    private AbstractAuthenticationToken toAuthentication(Jwt jwt) {
        try {
            Long userId = Long.valueOf(jwt.getSubject());
            return new UsernamePasswordAuthenticationToken(userId, jwt, List.of());
        } catch (NumberFormatException e) {
            throw new InvalidBearerTokenException("Token subject is not a user id");
        }
    }

    private static RSAPublicKey parsePublicKey(String base64Pem) {
        try {
            String pem = new String(Base64.getDecoder().decode(base64Pem.trim()), StandardCharsets.UTF_8);
            String body = pem
                    .replaceAll("-----BEGIN [A-Z ]+-----", "")
                    .replaceAll("-----END [A-Z ]+-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(body);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException(
                    "app.jwt.public-key is not a Base64-encoded RSA public key in PEM format", e);
        }
    }
}
