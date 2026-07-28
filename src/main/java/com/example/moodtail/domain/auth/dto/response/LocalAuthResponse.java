package com.example.moodtail.domain.auth.dto.response;

import com.example.moodtail.global.config.security.jwt.TokenInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record LocalAuthResponse(
        @Schema(description = "회원 사용자 ID", example = "37")
        Long userId,

        @Schema(description = "로컬 계정 이메일", format = "email", example = "user@example.com")
        String email,

        @Schema(description = "회원 닉네임", example = "무드테일")
        String nickname,

        @Schema(description = "회원가입 응답이면 true, 로그인 응답이면 false", example = "false")
        boolean isNewUser,

        @Schema(description = "Access Token 인증 방식", example = "Bearer")
        String grantType,

        @Schema(description = "회원 Access Token", example = "member-access-token")
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
