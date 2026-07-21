package com.example.moodtail.domain.auth.model;

import com.example.moodtail.domain.auth.entity.LocalAccount;
import com.example.moodtail.domain.user.entity.UserRole;

public record LocalAuthUser(
        Long userId,
        UserRole role,
        String email,
        String nickname
) {

    public static LocalAuthUser from(LocalAccount account) {
        return new LocalAuthUser(
                account.getUser().getId(),
                account.getUser().getRole(),
                account.getEmail(),
                account.getUser().getNickname()
        );
    }
}
