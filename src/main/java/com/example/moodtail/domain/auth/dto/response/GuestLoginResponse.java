package com.example.moodtail.domain.auth.dto.response;

import com.example.moodtail.global.config.security.jwt.TokenInfo;

public record GuestLoginResponse(
        Long userId,
        String guestUuid,
        boolean isNewUser,
        String grantType,
        String accessToken
) {

    public static GuestLoginResponse of(
            Long userId,
            String guestUuid,
            boolean isNewUser,
            TokenInfo tokenInfo
    ) {
        return new GuestLoginResponse(
                userId,
                guestUuid,
                isNewUser,
                "Bearer",
                tokenInfo.accessToken()
        );
    }
}
