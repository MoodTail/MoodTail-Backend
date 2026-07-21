package com.example.moodtail.domain.auth.model;

import com.example.moodtail.global.config.security.jwt.TokenInfo;

public record LocalAuthenticationResult(
        LocalAuthUser user,
        TokenInfo tokenInfo
) {
}
