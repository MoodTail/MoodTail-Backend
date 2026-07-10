package com.example.moodtail.domain.user.dto.response;

import com.example.moodtail.domain.user.enums.SocialProvider;
import com.example.moodtail.global.config.security.jwt.TokenInfo;

public record SocialLoginResponse(
        Long userId,
        String email,
        String nickname,
        SocialProvider provider,
        boolean isNewUser,
        String grantType,
        String accessToken
) {

    public static SocialLoginResponse of(
            Long userId,
            String nickname,
            SocialProvider provider,
            String socialEmail,
            TokenInfo tokenInfo,
            boolean isNewUser
    ) {
        return new SocialLoginResponse(
                userId,
                socialEmail,
                nickname,
                provider,
                isNewUser,
                "Bearer",
                tokenInfo.accessToken()
        );
    }
}
