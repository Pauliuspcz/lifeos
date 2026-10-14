package no.kristiania.lifeos.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthRegisterTest extends AbstractIntegrationTest {

    @Test
    void register_withValidBody_returns201_andNoPasswordInResponse() throws Exception {
        postJson("/auth/register", """
                {"email":"kari@example.com","password":"password123","name":"Kari"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("kari@example.com"))
                .andExpect(jsonPath("$.name").value("Kari"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void register_storesBcryptHash_notPlainPassword() throws Exception {
        register("kari@example.com", PASSWORD);
        String stored = jdbcTemplate.queryForObject(
                "select password_hash from users where email = ?", String.class, "kari@example.com");
        assertThat(stored).startsWith("$2").isNotEqualTo(PASSWORD);
    }

    @Test
    void register_lowercasesEmail() throws Exception {
        postJson("/auth/register", """
                {"email":"KARI@EXAMPLE.COM","password":"password123","name":"Kari"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("kari@example.com"));
    }

    @Test
    void register_duplicateEmail_returns409_evenWithDifferentCase() throws Exception {
        register("kari@example.com", PASSWORD);
        postJson("/auth/register", """
                {"email":"KARI@example.com","password":"password123","name":"Other"}""")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already registered"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void register_withEmptyBody_returns400_withFieldErrors() throws Exception {
        postJson("/auth/register", "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors", allOf(
                        hasEntry("email", "must not be blank"),
                        hasEntry("password", "must not be blank"),
                        hasEntry("name", "must not be blank"))))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void register_withInvalidEmail_returns400() throws Exception {
        postJson("/auth/register", """
                {"email":"not-an-email","password":"password123","name":"Kari"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").value("must be a well-formed email address"));
    }

    @Test
    void register_withPasswordTooShortOrTooLong_returns400() throws Exception {
        postJson("/auth/register", """
                {"email":"a@example.com","password":"short","name":"Kari"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", allOf(
                        hasEntry("password", "size must be between 8 and 72"))));
        postJson("/auth/register", """
                {"email":"a@example.com","password":"%s","name":"Kari"}""".formatted("x".repeat(73)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withMalformedJson_returns400() throws Exception {
        postJson("/auth/register", "{")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
