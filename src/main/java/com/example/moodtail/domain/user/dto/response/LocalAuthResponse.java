package com.example.moodtail.domain.user.dto.response;

import com.example.moodtail.global.config.security.jwt.TokenInfo;

public record LocalAuthResponse(
        Long userId,
        String email,
        String nickname,
        boolean isNewUser,
        String grantType,
        String accessToken
) {
    public static LocalAuthResponse of(
            Long userId,
            String email,
            String nickname,
            boolean isNewUser,
            TokenInfo tokenInfo
    ) {
        return new LocalAuthResponse(
                userId,
                email,
                nickname,
                isNewUser,
                "Bearer",
                tokenInfo.accessToken()
        );
    }
}
