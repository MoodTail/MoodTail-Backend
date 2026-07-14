package com.example.moodtail.global.auth.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthPropertiesTest {

    @Test
    void rejectsInsecureSameSiteNoneCookie() {
        assertThatThrownBy(() -> new AuthProperties.RefreshCookie(
                "refreshToken",
                "/",
                null,
                false,
                "None"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonPositiveOperationalTimeout() {
        assertThatThrownBy(() -> new AuthProperties.OAuth(
                300_000L,
                0L,
                5_000L,
                new AuthProperties.RateLimit(10, 60_000L),
                provider(),
                provider()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInsecureOAuthProviderEndpoint() {
        assertThatThrownBy(() -> new AuthProperties.Provider(
                true,
                "client-id",
                "client-secret",
                "https://frontend.example.com/callback",
                "http://provider.example.com/token",
                "https://provider.example.com/userinfo"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTPS");
    }

    @Test
    void allowsHttpRedirectOnlyForLoopbackDevelopmentHosts() {
        new AuthProperties.Provider(
                true,
                "client-id",
                "client-secret",
                "http://localhost:5173/auth/callback",
                "https://provider.example.com/token",
                "https://provider.example.com/userinfo"
        );

        assertThatThrownBy(() -> new AuthProperties.Provider(
                true,
                "client-id",
                "client-secret",
                "http://frontend.example.com/auth/callback",
                "https://provider.example.com/token",
                "https://provider.example.com/userinfo"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTPS");
    }

    @Test
    void rejectsRedirectUriContainingFragmentOrCredentials() {
        assertThatThrownBy(() -> new AuthProperties.Provider(
                true,
                "client-id",
                "client-secret",
                "https://frontend.example.com/auth/callback#token",
                "https://provider.example.com/token",
                "https://provider.example.com/userinfo"
        )).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new AuthProperties.Provider(
                true,
                "client-id",
                "client-secret",
                "https://user:password@frontend.example.com/auth/callback",
                "https://provider.example.com/token",
                "https://provider.example.com/userinfo"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    private AuthProperties.Provider provider() {
        return new AuthProperties.Provider(
                true,
                "client-id",
                "client-secret",
                "https://frontend.example.com/callback",
                "https://provider.example.com/token",
                "https://provider.example.com/userinfo"
        );
    }
}
