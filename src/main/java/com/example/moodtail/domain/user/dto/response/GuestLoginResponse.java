package com.example.moodtail.domain.user.dto.response;

import com.example.moodtail.domain.user.service.GuestLoginUser;
import com.example.moodtail.global.config.security.jwt.TokenInfo;

public record GuestLoginResponse(
        Long userId,
        String guestUuid,
        boolean isNewUser,
        String grantType,
        String accessToken
) {

    public static GuestLoginResponse of(GuestLoginUser guestLoginUser, TokenInfo tokenInfo) {
        return new GuestLoginResponse(
                guestLoginUser.userId(),
                guestLoginUser.guestUuid(),
                guestLoginUser.isNewUser(),
                "Bearer",
                tokenInfo.accessToken()
        );
    }
}
