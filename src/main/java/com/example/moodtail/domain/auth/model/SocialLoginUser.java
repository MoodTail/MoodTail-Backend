package com.example.moodtail.domain.auth.model;

import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.domain.user.entity.UserRole;

public record SocialLoginUser(
        Long userId,
        UserRole role,
        String nickname,
        SocialProvider provider,
        String socialEmail,
        boolean isNewUser
) {
}
