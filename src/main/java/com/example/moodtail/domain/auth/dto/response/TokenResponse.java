package com.example.moodtail.domain.auth.dto.response;

import com.example.moodtail.global.config.security.jwt.TokenInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(description = "Access Token 인증 방식", example = "Bearer")
        String grantType,

        @Schema(description = "재발급된 Access Token", example = "new-access-token")
        String accessToken
) {

    public static TokenResponse from(TokenInfo tokenInfo) {
        return new TokenResponse("Bearer", tokenInfo.accessToken());
    }
}
