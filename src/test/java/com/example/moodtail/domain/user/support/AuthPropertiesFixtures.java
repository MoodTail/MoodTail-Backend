package com.example.moodtail.domain.user.support;

import com.example.moodtail.domain.user.config.AuthProperties;

public final class AuthPropertiesFixtures {

    private AuthPropertiesFixtures() {
    }

    public static AuthProperties defaults() {
        return new AuthProperties(
                new AuthProperties.OAuth(
                        300_000L,
                        3_000L,
                        5_000L,
                        kakaoProvider()
                ),
                new AuthProperties.RefreshCookie("refreshToken", "/", null, false, "Lax"),
                new AuthProperties.GuestLogin(
                        "",
                        "게스트",
                        new AuthProperties.RateLimit(10, 60_000L),
                        new AuthProperties.RateLimit(60, 60_000L)
                ),
                new AuthProperties.Concurrency(10_000L, 25L, 3_000L, 3),
                new AuthProperties.RedisKeys("moodtail:auth:test:")
        );
    }

    public static AuthProperties.Provider kakaoProvider() {
        return new AuthProperties.Provider(
                true,
                "kakao-client-id",
                "kakao-client-secret",
                "http://frontend/kakao/callback",
                "https://kauth.kakao.com/oauth/token",
                "https://kapi.kakao.com/v2/user/me"
        );
    }

}
