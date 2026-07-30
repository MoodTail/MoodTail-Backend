package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.domain.user.util.InviteCodeGenerator;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.INVALID_ROLE;
import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.LOGIN_USER_REQUIRED;
import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.USER_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.UserErrorStatus.INVITE_CODE_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InviteCodeService {

    private final UserRepository userRepository;
    private final InviteCodeGenerator inviteCodeGenerator;

    @Transactional
    public String getOrIssueInviteCode(Long userId, String role) {
        validateRole(role);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(USER_NOT_FOUND));

        if (user.getInviteCode() != null) {
            return user.getInviteCode();
        }

        String inviteCode = inviteCodeGenerator.generateUnique();
        user.assignInviteCode(inviteCode);
        return inviteCode;
    }

    public User findUserByInviteCode(String inviteCode) {
        return userRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RestApiException(INVITE_CODE_NOT_FOUND));
    }

    private void validateRole(String role) {
        if (UserRole.GUEST.name().equals(role)) {
            throw new RestApiException(LOGIN_USER_REQUIRED);
        }
        if (!UserRole.USER.name().equals(role) && !UserRole.ADMIN.name().equals(role)) {
            throw new RestApiException(INVALID_ROLE);
        }
    }
}
