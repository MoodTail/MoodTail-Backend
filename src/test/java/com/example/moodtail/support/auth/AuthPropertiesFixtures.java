package com.example.moodtail.support.auth;

import com.example.moodtail.global.auth.config.AuthProperties;

public final class AuthPropertiesFixtures {

    private AuthPropertiesFixtures() {
    }

    public static AuthProperties defaults() {
        return new AuthProperties(
                new AuthProperties.OAuth(
                        300_000L,
                        600_000L,
                        3_000L,
                        5_000L,
                        new AuthProperties.RateLimit(10, 60_000L),
                        kakaoProvider(),
                        googleProvider()
                ),
                new AuthProperties.RefreshCookie("refreshToken", null, false, "Lax"),
                new AuthProperties.GuestLogin(
                        "게스트",
                        new AuthProperties.RateLimit(10, 60_000L),
                        new AuthProperties.RateLimit(60, 60_000L)
                ),
                new AuthProperties.RedisKeys("moodtail:auth:test:")
        );
    }

    public static AuthProperties.Provider kakaoProvider() {
        return new AuthProperties.Provider(
                true,
                "kakao-client-id",
                "kakao-client-secret",
                "http://localhost:5173/auth/kakao/callback",
                "https://kauth.kakao.com/oauth/token",
                "https://kapi.kakao.com/v2/user/me"
        );
    }

    public static AuthProperties.Provider googleProvider() {
        return new AuthProperties.Provider(
                true,
                "google-client-id",
                "google-client-secret",
                "http://localhost:5173/auth/google/callback",
                "https://oauth2.googleapis.com/token",
                "https://openidconnect.googleapis.com/v1/userinfo"
        );
    }
}
