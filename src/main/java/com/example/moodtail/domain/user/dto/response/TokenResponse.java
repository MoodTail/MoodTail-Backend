package com.example.moodtail.domain.user.dto.response;

import com.example.moodtail.global.config.security.jwt.TokenInfo;

public record TokenResponse(
        String grantType,
        String accessToken
) {

    public static TokenResponse from(TokenInfo tokenInfo) {
        return new TokenResponse("Bearer", tokenInfo.accessToken());
    }
}
