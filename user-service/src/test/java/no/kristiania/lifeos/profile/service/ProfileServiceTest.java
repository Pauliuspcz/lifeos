package no.kristiania.lifeos.profile.service;

import no.kristiania.lifeos.exception.NotFoundException;
import no.kristiania.lifeos.profile.dto.ProfileResponse;
import no.kristiania.lifeos.profile.dto.UpdateProfileRequest;
import no.kristiania.lifeos.user.User;
import no.kristiania.lifeos.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks ProfileService service;

    private User givenUser(String picture) {
        User user = new User();
        user.setId(1L);
        user.setEmail("kari@example.com");
        user.setName("Kari");
        user.setProfilePictureUrl(picture);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void getProfile_returnsTheUsersData() {
        givenUser("https://example.com/me.png");

        ProfileResponse response = service.getProfile(1L);

        assertThat(response).isEqualTo(
                new ProfileResponse(1L, "kari@example.com", "Kari", "https://example.com/me.png"));
    }

    @Test
    void getProfile_unknownUser_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile(99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateProfile_unknownUser_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateProfile(99L, new UpdateProfileRequest("New", null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateProfile_trimsNameAndPictureUrl() {
        givenUser(null);

        ProfileResponse response = service.updateProfile(1L,
                new UpdateProfileRequest("  Kari Nordmann  ", "  https://example.com/me.png  "));

        assertThat(response.name()).isEqualTo("Kari Nordmann");
        assertThat(response.profilePictureUrl()).isEqualTo("https://example.com/me.png");
    }

    @Test
    void updateProfile_nullFieldsLeaveValuesUnchanged() {
        User user = givenUser("https://example.com/me.png");

        service.updateProfile(1L, new UpdateProfileRequest(null, null));

        assertThat(user.getName()).isEqualTo("Kari");
        assertThat(user.getProfilePictureUrl()).isEqualTo("https://example.com/me.png");
    }

    @Test
    void updateProfile_emptyPictureUrl_removesPicture() {
        User user = givenUser("https://example.com/me.png");

        service.updateProfile(1L, new UpdateProfileRequest(null, ""));

        assertThat(user.getProfilePictureUrl()).isNull();
    }

    @Test
    void updateProfile_blankPictureUrl_removesPicture() {
        User user = givenUser("https://example.com/me.png");

        service.updateProfile(1L, new UpdateProfileRequest(null, "   "));

        assertThat(user.getProfilePictureUrl()).isNull();
    }

    @Test
    void updateProfile_changingOnlyName_keepsPicture() {
        User user = givenUser("https://example.com/me.png");

        service.updateProfile(1L, new UpdateProfileRequest("New name", null));

        assertThat(user.getName()).isEqualTo("New name");
        assertThat(user.getProfilePictureUrl()).isEqualTo("https://example.com/me.png");
    }
}
