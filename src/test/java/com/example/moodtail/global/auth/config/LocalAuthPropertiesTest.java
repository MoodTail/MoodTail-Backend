package com.example.moodtail.global.auth.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalAuthPropertiesTest {

    @Test
    void bcryptPasswordPolicyRejectsMoreThan72Bytes() {
        assertThatThrownBy(() -> new LocalAuthProperties.Password(8, 73))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void enabledPasswordResetRequiresSender() {
        assertThatThrownBy(() -> new LocalAuthProperties.PasswordReset(
                true,
                "",
                "Password reset",
                "Code: %s",
                "password-reset-pepper-with-at-least-32-bytes",
                300_000L,
                600_000L,
                60_000L,
                5,
                new AuthProperties.RateLimit(10, 600_000L)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sender");
    }

    @Test
    void enabledPasswordResetRequiresPepper() {
        assertThatThrownBy(() -> new LocalAuthProperties.PasswordReset(
                true,
                "no-reply@example.com",
                "Password reset",
                "Code: %s",
                "",
                300_000L,
                600_000L,
                60_000L,
                5,
                new AuthProperties.RateLimit(10, 600_000L)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pepper");
    }

    @Test
    void disabledPasswordResetAllowsMissingDeploymentSecrets() {
        new LocalAuthProperties.PasswordReset(
                false,
                "",
                "Password reset",
                "Code: %s",
                "",
                300_000L,
                600_000L,
                60_000L,
                5,
                new AuthProperties.RateLimit(10, 600_000L)
        );
    }

    @Test
    void enabledPasswordResetRequiresAtLeast32BytePepper() {
        assertThatThrownBy(() -> new LocalAuthProperties.PasswordReset(
                true,
                "no-reply@example.com",
                "Password reset",
                "Code: %s",
                "short-pepper",
                300_000L,
                600_000L,
                60_000L,
                5,
                new AuthProperties.RateLimit(10, 600_000L)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    void enabledPasswordResetRejectsInvalidMailBodyFormatAtStartup() {
        assertThatThrownBy(() -> new LocalAuthProperties.PasswordReset(
                true,
                "no-reply@example.com",
                "Password reset",
                "Code: %s %s",
                "password-reset-pepper-with-at-least-32-bytes",
                300_000L,
                600_000L,
                60_000L,
                5,
                new AuthProperties.RateLimit(10, 600_000L)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid format");
    }

    @Test
    void enabledPasswordResetRejectsEscapedPlaceholderThatDoesNotRenderCode() {
        assertThatThrownBy(() -> new LocalAuthProperties.PasswordReset(
                true,
                "no-reply@example.com",
                "Password reset",
                "Code: %%s",
                "password-reset-pepper-with-at-least-32-bytes",
                300_000L,
                600_000L,
                60_000L,
                5,
                new AuthProperties.RateLimit(10, 600_000L)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("render the verification code");
    }
}
