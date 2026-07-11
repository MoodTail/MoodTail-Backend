package com.example.moodtail.domain.user.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalAuthPropertiesTest {

    @Test
    void enabledPasswordResetRequiresPepperAndSender() {
        assertThatThrownBy(() -> new LocalAuthProperties.PasswordReset(
                true,
                "",
                "Password reset",
                "Code: %s",
                "",
                300_000L,
                600_000L,
                60_000L,
                5,
                "",
                new AuthProperties.RateLimit(10, 600_000L)
        )).isInstanceOf(IllegalArgumentException.class);
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
                "",
                new AuthProperties.RateLimit(10, 600_000L)
        );
    }
}
