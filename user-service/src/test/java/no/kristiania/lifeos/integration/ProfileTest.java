package no.kristiania.lifeos.integration;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProfileTest extends AbstractIntegrationTest {

    private ResultActions patchProfile(String token, String body) throws Exception {
        return mockMvc.perform(authed(patch("/profile/me"), token)
                .contentType(APPLICATION_JSON).content(body));
    }

    @Test
    void getMe_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/profile/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_withGarbageToken_returns401() throws Exception {
        mockMvc.perform(authed(get("/profile/me"), "not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_withValidToken_returnsOwnProfile() throws Exception {
        Tokens tokens = registerAndLogin("kari@example.com");

        mockMvc.perform(authed(get("/profile/me"), tokens.access()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("kari@example.com"))
                .andExpect(jsonPath("$.name").value("Kari"))
                .andExpect(jsonPath("$.profilePictureUrl").value(nullValue()));
    }

    @Test
    void getMe_returnsTheCorrectUser_whenSeveralExist() throws Exception {
        registerAndLogin("one@example.com");
        Tokens second = registerAndLogin("two@example.com");

        mockMvc.perform(authed(get("/profile/me"), second.access()))
                .andExpect(jsonPath("$.email").value("two@example.com"));
    }

    @Test
    void getMe_whenUserWasDeleted_returns404() throws Exception {
        Tokens tokens = registerAndLogin("kari@example.com");
        jdbcTemplate.update("delete from users");

        mockMvc.perform(authed(get("/profile/me"), tokens.access()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void patchMe_updatesNameAndPicture() throws Exception {
        Tokens tokens = registerAndLogin("kari@example.com");

        patchProfile(tokens.access(), """
                {"name":"Kari Nordmann","profilePictureUrl":"https://example.com/me.png"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Kari Nordmann"))
                .andExpect(jsonPath("$.profilePictureUrl").value("https://example.com/me.png"));
    }

    @Test
    void patchMe_omittedFieldsStayUnchanged() throws Exception {
        Tokens tokens = registerAndLogin("kari@example.com");
        patchProfile(tokens.access(), """
                {"profilePictureUrl":"https://example.com/me.png"}""").andExpect(status().isOk());

        patchProfile(tokens.access(), """
                {"name":"New name"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"))
                .andExpect(jsonPath("$.profilePictureUrl").value("https://example.com/me.png"));
    }

    @Test
    void patchMe_emptyPictureString_removesPicture() throws Exception {
        Tokens tokens = registerAndLogin("kari@example.com");
        patchProfile(tokens.access(), """
                {"profilePictureUrl":"https://example.com/me.png"}""").andExpect(status().isOk());

        patchProfile(tokens.access(), """
                {"profilePictureUrl":""}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePictureUrl").value(nullValue()));
    }

    @Test
    void patchMe_withBlankOrTooLongValues_returns400() throws Exception {
        Tokens tokens = registerAndLogin("kari@example.com");

        patchProfile(tokens.access(), """
                {"name":"   "}""").andExpect(status().isBadRequest());
        patchProfile(tokens.access(), """
                {"name":"%s"}""".formatted("a".repeat(256))).andExpect(status().isBadRequest());
        patchProfile(tokens.access(), """
                {"profilePictureUrl":"%s"}""".formatted("a".repeat(501))).andExpect(status().isBadRequest());
    }

    @Test
    void patchMe_withoutToken_returns401() throws Exception {
        mockMvc.perform(patch("/profile/me").contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
