package com.example.moodtail.domain.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatException;

@ConfigurationProperties(prefix = "auth.local")
public record LocalAuthProperties(
        Password password,
        Login login,
        PasswordReset passwordReset
) {

    private static final String MAIL_TEMPLATE_CODE_SENTINEL = "MOODTAIL_RESET_CODE_849201";

    public LocalAuthProperties {
        if (password == null || login == null || passwordReset == null) {
            throw new IllegalArgumentException("All local-auth configuration groups are required");
        }
    }

    public record Password(int minLength, int maxBytes) {
        public Password {
            if (minLength < 8 || maxBytes < minLength || maxBytes > 72) {
                throw new IllegalArgumentException("Invalid local password length policy");
            }
        }
    }

    public record Login(int maxFailedAttempts, long lockDurationMillis) {
        public Login {
            if (maxFailedAttempts < 1 || lockDurationMillis < 1) {
                throw new IllegalArgumentException("Invalid local login lock policy");
            }
        }
    }

    public record PasswordReset(
            boolean enabled,
            String sender,
            String subject,
            String bodyTemplate,
            String pepper,
            long codeExpirationMillis,
            long tokenExpirationMillis,
            long resendCooldownMillis,
            int maxVerificationAttempts,
            String clientIpHeader,
            AuthProperties.RateLimit clientRateLimit
    ) {
        public PasswordReset {
            if (codeExpirationMillis < 1 || tokenExpirationMillis < 1 || resendCooldownMillis < 1
                    || maxVerificationAttempts < 1 || clientRateLimit == null) {
                throw new IllegalArgumentException("Invalid password-reset policy");
            }
            if (enabled) {
                requireText(sender, "Password-reset sender");
                requireText(subject, "Password-reset subject");
                requireText(bodyTemplate, "Password-reset body template");
                requireText(pepper, "Password-reset pepper");
                if (pepper.getBytes(StandardCharsets.UTF_8).length < 32) {
                    throw new IllegalArgumentException("Password-reset pepper must be at least 32 bytes");
                }
                String renderedBody;
                try {
                    renderedBody = bodyTemplate.formatted(MAIL_TEMPLATE_CODE_SENTINEL);
                } catch (IllegalFormatException exception) {
                    throw new IllegalArgumentException("Password-reset body template has an invalid format", exception);
                }
                if (!renderedBody.contains(MAIL_TEMPLATE_CODE_SENTINEL)) {
                    throw new IllegalArgumentException("Password-reset body template must render the verification code");
                }
            }
        }
    }

    private static void requireText(String value, String name) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
