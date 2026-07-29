package com.example.moodtail.domain.auth.dto.response;

import com.example.moodtail.domain.auth.model.GuestLoginUser;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record GuestLoginResponse(
        @Schema(description = "게스트 사용자 ID", example = "101")
        Long userId,

        @Schema(
                description = "요청에 사용한 게스트 UUID",
                format = "uuid",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        String guestUuid,

        @Schema(description = "이번 요청에서 새 게스트 사용자를 생성했는지 여부", example = "true")
        boolean isNewUser,

        @Schema(description = "Access Token 인증 방식", example = "Bearer")
        String grantType,

        @Schema(description = "게스트 Access Token", example = "guest-access-token")
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
