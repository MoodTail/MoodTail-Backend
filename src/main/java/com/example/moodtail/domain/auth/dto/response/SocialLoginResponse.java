package com.example.moodtail.domain.auth.dto.response;

import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record SocialLoginResponse(
        @Schema(
                description = "소셜 인증 처리 상태",
                allowableValues = {"LOGIN_COMPLETED", "SIGNUP_REQUIRED", "SIGNUP_COMPLETED"},
                example = "LOGIN_COMPLETED"
        )
        Status status,

        @Schema(
                description = "로그인·가입 완료 사용자 ID. SIGNUP_REQUIRED이면 null입니다.",
                example = "37"
        )
        Long userId,

        @Schema(
                description = "소셜 제공자가 확인한 이메일",
                format = "email",
                example = "social-user@example.com"
        )
        String email,

        @Schema(description = "회원 닉네임. SIGNUP_REQUIRED이면 null입니다.", example = "무드테일")
        String nickname,

        @Schema(description = "소셜 로그인 제공자", allowableValues = {"KAKAO", "GOOGLE"}, example = "KAKAO")
        SocialProvider provider,

        @Schema(
                description = "신규 가입 완료 API에서 사용할 43자 일회성 토큰. "
                        + "SIGNUP_REQUIRED일 때만 반환합니다.",
                example = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        )
        String signupToken,

        @Schema(
                description = "소셜 가입 토큰 유효 시간(초). SIGNUP_REQUIRED일 때만 반환합니다.",
                example = "600"
        )
        Long signupTokenExpiresInSeconds,

        @Schema(
                description = "Access Token 인증 방식. 로그인이 완료되지 않았으면 null입니다.",
                example = "Bearer"
        )
        String grantType,

        @Schema(
                description = "회원 Access Token. 로그인이 완료되지 않았으면 null입니다.",
                example = "member-access-token"
        )
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
