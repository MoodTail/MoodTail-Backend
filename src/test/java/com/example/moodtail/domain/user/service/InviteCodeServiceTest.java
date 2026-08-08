package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.domain.user.util.InviteCodeGenerator;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.UserErrorStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InviteCodeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InviteCodeGenerator inviteCodeGenerator;

    @InjectMocks
    private InviteCodeService inviteCodeService;

    @Test
    @DisplayName("초대 코드 형식이 올바르지 않으면 조회 없이 예외를 던진다")
    void throwsInvalidFormatWhenInviteCodeDoesNotMatchIssuedFormat() {
        when(inviteCodeGenerator.matchesFormat("INVALID-CODE")).thenReturn(false);

        assertThatThrownBy(() -> inviteCodeService.findUserByInviteCode("INVALID-CODE"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode())
                                .isEqualTo(UserErrorStatus.INVALID_INVITE_CODE_FORMAT.getCode().getCode())
                );
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("형식이 올바른 초대 코드는 사용자 조회를 시도한다")
    void findsUserWhenInviteCodeMatchesIssuedFormat() {
        User user = User.createGuest("guest-1", "닉네임", java.time.LocalDateTime.now());
        when(inviteCodeGenerator.matchesFormat("MOOD-4821")).thenReturn(true);
        when(userRepository.findByInviteCode("MOOD-4821")).thenReturn(Optional.of(user));

        User found = inviteCodeService.findUserByInviteCode("MOOD-4821");

        assertThat(found).isEqualTo(user);
    }

    @Test
    @DisplayName("형식은 올바르지만 존재하지 않는 초대 코드는 코드 없음 예외를 던진다")
    void throwsInviteCodeNotFoundWhenNoUserMatches() {
        when(inviteCodeGenerator.matchesFormat("MOOD-4821")).thenReturn(true);
        when(userRepository.findByInviteCode("MOOD-4821")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inviteCodeService.findUserByInviteCode("MOOD-4821"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode())
                                .isEqualTo(UserErrorStatus.INVITE_CODE_NOT_FOUND.getCode().getCode())
                );
    }
}
