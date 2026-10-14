package no.kristiania.lifeos.integration;

import no.kristiania.lifeos.auth.google.GoogleUser;
import no.kristiania.lifeos.exception.InvalidGoogleTokenException;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthGoogleTest extends AbstractIntegrationTest {

    private void givenGoogleUser(String idToken, GoogleUser user) {
        when(googleTokenVerifier.verify(idToken)).thenReturn(user);
    }

    private ResultActions googleLogin(String idToken) throws Exception {
        return postJson("/auth/google", """
                {"idToken":"%s"}""".formatted(idToken));
    }

    @Test
    void google_withNewUser_createsAccount_andReturnsTokens() throws Exception {
        givenGoogleUser("valid", new GoogleUser("g-1", "nina@example.com", "Nina", "https://pic.verySexyPic.com/n.png"));

        Tokens tokens = tokensFrom(googleLogin("valid")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyString())))
                .andExpect(jsonPath("$.refreshToken", not(emptyString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn());

        profileOf(tokens)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nina@example.com"))
                .andExpect(jsonPath("$.name").value("Nina"))
                .andExpect(jsonPath("$.profilePictureUrl").value("https://pic.verySexyPic.com/n.png"));
        assertThat(userCount()).isEqualTo(1);
    }

    @Test
    void google_withSameGoogleAccountTwice_logsInSameUser_withoutDuplicate() throws Exception {
        givenGoogleUser("valid", new GoogleUser("g-1", "nina@example.com", "Nina", null));

        googleLogin("valid").andExpect(status().isOk());
        googleLogin("valid").andExpect(status().isOk());

        assertThat(userCount()).isEqualTo(1);
    }

    @Test
    void google_withExistingEmail_linksAccount_insteadOfCreatingNewUser() throws Exception {
        register("kari@example.com", PASSWORD);
        givenGoogleUser("valid", new GoogleUser("g-1", "Kari@Example.com", "Kari G", "https://pic.notSoSexyPic.com/k.png"));

        googleLogin("valid").andExpect(status().isOk());

        assertThat(userCount()).isEqualTo(1);
        String googleId = jdbcTemplate.queryForObject(
                "select google_id from users where email = ?", String.class, "kari@example.com");
        assertThat(googleId).isEqualTo("g-1");
        postJson("/auth/login", credentials("kari@example.com", PASSWORD)).andExpect(status().isOk());
    }

    @Test
    void google_linking_fillsPictureWhenEmpty_butKeepsExistingPicture() throws Exception {
        register("empty@example.com", PASSWORD);
        givenGoogleUser("one", new GoogleUser("g-1", "empty@example.com", "A", "https://google.example.com/a.png"));
        Tokens emptyUser = tokensFrom(googleLogin("one").andExpect(status().isOk()).andReturn());
        profileOf(emptyUser).andExpect(jsonPath("$.profilePictureUrl").value("https://google.example.com/a.png"));

        Tokens withPicture = registerAndLogin("pic@example.com");
        mockMvc.perform(patch("/profile/me")
                        .header("Authorization", "Bearer " + withPicture.access())
                        .contentType(APPLICATION_JSON)
                        .content("""
                        {"profilePictureUrl":"https://mine.example.com/me.png"}"""))
                .andExpect(status().isOk());
        givenGoogleUser("two", new GoogleUser("g-2", "pic@example.com", "B", "https://google.example.com/b.png"));

        googleLogin("two").andExpect(status().isOk());

        profileOf(withPicture).andExpect(jsonPath("$.profilePictureUrl").value("https://mine.example.com/me.png"));
    }

    @Test
    void google_withEmailLinkedToDifferentGoogleAccount_returns409() throws Exception {
        givenGoogleUser("first", new GoogleUser("g-1", "nina@example.com", "Nina", null));
        googleLogin("first").andExpect(status().isOk());
        givenGoogleUser("second", new GoogleUser("g-2", "nina@example.com", "Nina", null));

        googleLogin("second")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void google_withInvalidToken_returns401() throws Exception {
        when(googleTokenVerifier.verify("bad")).thenThrow(new InvalidGoogleTokenException());

        googleLogin("bad")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        assertThat(userCount()).isZero();
    }

    @Test
    void google_withMissingIdToken_returns400() throws Exception {
        postJson("/auth/google", "{}").andExpect(status().isBadRequest());
    }

    @Test
    void google_withoutName_usesEmailPrefixAsName() throws Exception {
        givenGoogleUser("valid", new GoogleUser("g-1", "no.name@example.com", null, null));

        Tokens tokens = tokensFrom(googleLogin("valid").andExpect(status().isOk()).andReturn());

        profileOf(tokens).andExpect(jsonPath("$.name").value("no.name"));
    }

    @Test
    void googleOnlyAccount_cannotLogInWithPassword() throws Exception {
        givenGoogleUser("valid", new GoogleUser("g-1", "nina@example.com", "Nina", null));
        googleLogin("valid").andExpect(status().isOk());

        postJson("/auth/login", credentials("nina@example.com", PASSWORD))
                .andExpect(status().isUnauthorized());
    }
}