package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.enums.UserRole;

public record GuestLoginUser(
        Long userId,
        String guestUuid,
        UserRole role,
        boolean isNewUser
) {

    public static GuestLoginUser from(User user, boolean isNewUser) {
        return new GuestLoginUser(
                user.getId(),
                user.getGuestUuid(),
                user.getRole(),
                isNewUser
        );
    }
}
