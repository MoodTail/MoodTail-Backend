package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.enums.SocialProvider;
import com.example.moodtail.domain.user.enums.UserRole;
public record SocialLoginUser(
        Long userId,
        UserRole role,
        String nickname,
        SocialProvider provider,
        String socialEmail,
        boolean isNewUser
) {
}
