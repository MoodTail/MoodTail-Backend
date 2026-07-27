package com.example.moodtail.domain.auth.dto.response;

import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.config.security.jwt.TokenInfo;

public record SocialLoginResponse(
        Status status,
        Long userId,
        String email,
        String nickname,
        SocialProvider provider,
        String signupToken,
        Long signupTokenExpiresInSeconds,
        String grantType,
        String accessToken
) {

    public static SocialLoginResponse loginCompleted(
            Long userId,
            String nickname,
            SocialProvider provider,
            String socialEmail,
            TokenInfo tokenInfo
    ) {
        return new SocialLoginResponse(
                Status.LOGIN_COMPLETED,
                userId,
                socialEmail,
                nickname,
                provider,
                null,
                null,
                "Bearer",
                tokenInfo.accessToken()
        );
    }

    public static SocialLoginResponse signupRequired(
            String email,
            SocialProvider provider,
            String signupToken,
            long signupTokenExpiresInSeconds
    ) {
        return new SocialLoginResponse(
                Status.SIGNUP_REQUIRED,
                null,
                email,
                null,
                provider,
                signupToken,
                signupTokenExpiresInSeconds,
                null,
                null
        );
    }

    public static SocialLoginResponse signupCompleted(
            Long userId,
            String nickname,
            SocialProvider provider,
            String socialEmail,
            TokenInfo tokenInfo
    ) {
        return new SocialLoginResponse(
                Status.SIGNUP_COMPLETED,
                userId,
                socialEmail,
                nickname,
                provider,
                null,
                null,
                "Bearer",
                tokenInfo.accessToken()
        );
    }

    public enum Status {
        LOGIN_COMPLETED,
        SIGNUP_REQUIRED,
        SIGNUP_COMPLETED
    }
}
