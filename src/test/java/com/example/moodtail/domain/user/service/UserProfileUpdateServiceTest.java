package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.collection.entity.UserUnlockedMoodType;
import com.example.moodtail.domain.collection.repository.UserUnlockedMoodTypeRepository;
import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.user.dto.request.UserProfileUpdateRequest;
import com.example.moodtail.domain.user.dto.response.UserProfileUpdateResponse;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserProfileUpdateServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserUnlockedMoodTypeRepository userUnlockedMoodTypeRepository;

    @InjectMocks
    private MyPageService myPageService;

    @Test
    void updateNickname() {
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        given(user.getNickname()).willReturn("updated");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserProfileUpdateResponse response = myPageService.updateProfile(
                1L,
                UserRole.USER.name(),
                new UserProfileUpdateRequest(" updated ", null)
        );

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.nickname()).isEqualTo("updated");
        org.mockito.Mockito.verify(user).updateNickname("updated");
    }

    @Test
    void updateNicknameAllowsFiftyUnicodeCodePoints() {
        String nickname = "😀".repeat(50);
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        given(user.getNickname()).willReturn(nickname);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserProfileUpdateResponse response = myPageService.updateProfile(
                1L,
                UserRole.USER.name(),
                new UserProfileUpdateRequest(nickname, null)
        );

        assertThat(response.nickname()).isEqualTo(nickname);
        org.mockito.Mockito.verify(user).updateNickname(nickname);
    }

    @Test
    void guestCannotUpdateProfile() {
        assertThatThrownBy(() -> myPageService.updateProfile(
                1L,
                UserRole.GUEST.name(),
                new UserProfileUpdateRequest("nickname", null)
        ))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH027")
                );

        verifyNoInteractions(userRepository);
    }

    @Test
    void rejectInvalidNickname() {
        assertThatThrownBy(() -> myPageService.updateProfile(
                1L,
                UserRole.USER.name(),
                new UserProfileUpdateRequest(" ", null)
        ))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("USER400")
                );

        verifyNoInteractions(userRepository);
    }

    @Test
    void rejectNicknameLongerThanFiftyUnicodeCodePoints() {
        assertThatThrownBy(() -> myPageService.updateProfile(
                1L,
                UserRole.USER.name(),
                new UserProfileUpdateRequest("😀".repeat(51), null)
        ))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("USER400")
                );

        verifyNoInteractions(userRepository);
    }

    @Test
    void updateRepresentativeMoodTypeWithUnlockedMoodType() {
        User user = mock(User.class);
        UserUnlockedMoodType unlockedMoodType = mock(UserUnlockedMoodType.class);
        MoodType moodType = mock(MoodType.class);
        Image image = mock(Image.class);
        given(user.getId()).willReturn(1L);
        given(user.getNickname()).willReturn("nickname");
        given(user.getRepresentativeMoodType()).willReturn(moodType);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userUnlockedMoodTypeRepository.findByUserIdAndMoodTypeId(1L, 2002L))
                .willReturn(Optional.of(unlockedMoodType));
        given(unlockedMoodType.getMoodType()).willReturn(moodType);
        given(moodType.getId()).willReturn(2002L);
        given(moodType.getCode()).willReturn("TYPE_1");
        given(moodType.getName()).willReturn("무드 타입 1");
        given(moodType.getCharacterImage()).willReturn(image);
        given(image.getImageUrl()).willReturn("https://cdn.moodtail.com/mock/types/type-1.png");

        UserProfileUpdateResponse response = myPageService.updateProfile(
                1L,
                UserRole.USER.name(),
                new UserProfileUpdateRequest(null, 2002L)
        );

        assertThat(response.nickname()).isEqualTo("nickname");
        assertThat(response.representativeMoodType().moodTypeId()).isEqualTo(2002L);
        assertThat(response.representativeMoodType().characterImageUrl())
                .isEqualTo("https://cdn.moodtail.com/mock/types/type-1.png");
    }
}
