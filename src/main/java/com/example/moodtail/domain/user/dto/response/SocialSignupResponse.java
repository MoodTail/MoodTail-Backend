package com.example.moodtail.domain.user.dto.response;

import com.example.moodtail.domain.user.enums.SocialProvider;
import com.example.moodtail.domain.user.service.SocialLoginUser;
import com.example.moodtail.global.config.security.jwt.TokenInfo;

public record SocialSignupResponse(
        Long userId,
        String email,
        String nickname,
        SocialProvider provider,
        boolean isNewUser,
        String grantType,
        String accessToken
) {

    public static SocialSignupResponse of(SocialLoginUser user, TokenInfo tokenInfo) {
        return new SocialSignupResponse(
                user.userId(),
                user.socialEmail(),
                user.nickname(),
                user.provider(),
                true,
                "Bearer",
                tokenInfo.accessToken()
        );
    }
}
