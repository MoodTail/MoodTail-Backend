package com.example.moodtail.domain.user.config;

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
