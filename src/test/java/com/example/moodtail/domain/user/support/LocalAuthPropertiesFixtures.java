package com.example.moodtail.domain.user.support;

import com.example.moodtail.domain.user.config.AuthProperties;
import com.example.moodtail.domain.user.config.LocalAuthProperties;

public final class LocalAuthPropertiesFixtures {

    private LocalAuthPropertiesFixtures() {
    }

    public static LocalAuthProperties enabled() {
        return new LocalAuthProperties(
                new LocalAuthProperties.Password(8, 72),
                new LocalAuthProperties.Login(5, 900_000L),
                new LocalAuthProperties.PasswordReset(
                        true,
                        "no-reply@example.com",
                        "Password reset",
                        "Code: %s",
                        "test-password-reset-pepper",
                        300_000L,
                        600_000L,
                        60_000L,
                        5,
                        "",
                        new AuthProperties.RateLimit(10, 600_000L)
                )
        );
    }
}
