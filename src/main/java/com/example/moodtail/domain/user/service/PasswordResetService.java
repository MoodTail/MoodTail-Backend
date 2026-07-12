package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.config.AuthProperties;
import com.example.moodtail.domain.user.config.LocalAuthProperties;
import com.example.moodtail.domain.user.dto.response.PasswordResetCodeResponse;
import com.example.moodtail.domain.user.dto.response.PasswordResetVerificationResponse;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.lock.IdentityLockManager;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;

import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.bestEffort;
import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.required;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int RESET_TOKEN_BYTES = 32;

    private final LocalAccountService localAccountService;
    private final PasswordResetMailSender mailSender;
    private final RedisRepository redisRepository;
    private final IdentityLockManager identityLockManager;
    private final LocalAuthProperties properties;

    public PasswordResetCodeResponse requestCode(String email, HttpServletRequest request) {
        LocalAuthProperties.PasswordReset reset = enabledPolicy();
        String normalizedEmail = localAccountService.normalizeEmail(email);
        String emailFingerprint = sha256(normalizedEmail);
        enforceRequestLimits(emailFingerprint, resolveClientAddress(request), reset);

        localAccountService.findPasswordResetAccount(normalizedEmail).ifPresent(account -> {
            String code = generateCode();
            try {
                required(
                        "save password-reset code",
                        () -> redisRepository.savePasswordResetCode(
                                emailFingerprint,
                                account.localAccountId(),
                                account.passwordVersion(),
                                codeDigest(normalizedEmail, code, reset.pepper()),
                                Duration.ofMillis(reset.codeExpirationMillis())
                        )
                );
                mailSender.sendCode(account.email(), code);
            } catch (RuntimeException exception) {
                bestEffort(
                        "delete password-reset code after delivery setup failure",
                        () -> redisRepository.deletePasswordResetCode(emailFingerprint)
                );
                bestEffort(
                        "release password-reset cooldown after delivery failure",
                        () -> redisRepository.deletePasswordResetCooldown(emailFingerprint)
                );
                throw exception;
            }
        });

        return new PasswordResetCodeResponse(Duration.ofMillis(reset.codeExpirationMillis()).toSeconds());
    }

    public PasswordResetVerificationResponse verifyCode(String email, String code) {
        LocalAuthProperties.PasswordReset reset = enabledPolicy();
        String normalizedEmail = localAccountService.normalizeEmail(email);
        RedisRepository.PasswordResetTokenSession session = required(
                "verify password-reset code",
                () -> redisRepository.verifyPasswordResetCode(
                        sha256(normalizedEmail),
                        codeDigest(normalizedEmail, code, reset.pepper()),
                        reset.maxVerificationAttempts()
                )
        ).orElseThrow(() -> new RestApiException(AuthErrorStatus.EMAIL_CODE_MISMATCH));

        String resetToken = randomToken();
        Duration tokenTtl = Duration.ofMillis(reset.tokenExpirationMillis());
        required(
                "save password-reset token",
                () -> redisRepository.savePasswordResetToken(sha256(resetToken), session, tokenTtl)
        );
        return new PasswordResetVerificationResponse(resetToken, tokenTtl.toSeconds());
    }

    public void changePassword(String resetToken, String password, String passwordConfirm) {
        enabledPolicy();
        localAccountService.validatePassword(password, passwordConfirm);
        identityLockManager.executeForPasswordResetToken(resetToken, () -> {
            RedisRepository.PasswordResetTokenSession session = required(
                    "consume password-reset token",
                    () -> redisRepository.consumePasswordResetToken(sha256(resetToken))
            ).orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_PASSWORD_RESET_TOKEN));

            localAccountService.changePassword(
                    session.localAccountId(),
                    session.passwordVersion(),
                    password,
                    passwordConfirm,
                    account -> required(
                            "delete refresh session after password reset",
                            () -> redisRepository.deleteRefreshJti(account.userId())
                    )
            );
            return null;
        });
    }

    private void enforceRequestLimits(
            String emailFingerprint,
            String clientAddress,
            LocalAuthProperties.PasswordReset reset
    ) {
        AuthProperties.RateLimit clientLimit = reset.clientRateLimit();
        boolean clientAllowed = required(
                "acquire password-reset client rate-limit slot",
                () -> redisRepository.acquirePasswordResetClientSlot(
                        sha256(clientAddress),
                        clientLimit.maxAttempts(),
                        Duration.ofMillis(clientLimit.windowMillis())
                )
        );
        if (!clientAllowed) {
            throw new RestApiException(AuthErrorStatus.TOO_MANY_PASSWORD_RESET_REQUESTS);
        }
        boolean emailAllowed = required(
                "acquire password-reset resend cooldown",
                () -> redisRepository.acquirePasswordResetCooldown(
                        emailFingerprint,
                        Duration.ofMillis(reset.resendCooldownMillis())
                )
        );
        if (!emailAllowed) {
            throw new RestApiException(AuthErrorStatus.TOO_MANY_PASSWORD_RESET_REQUESTS);
        }
    }

    private LocalAuthProperties.PasswordReset enabledPolicy() {
        LocalAuthProperties.PasswordReset reset = properties.passwordReset();
        if (!reset.enabled()) {
            throw new RestApiException(AuthErrorStatus.PASSWORD_RESET_DISABLED);
        }
        return reset;
    }

    private String resolveClientAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String configuredHeader = properties.passwordReset().clientIpHeader();
        if (StringUtils.hasText(configuredHeader)) {
            String forwarded = request.getHeader(configuredHeader);
            if (StringUtils.hasText(forwarded)) {
                String firstAddress = forwarded.split(",", 2)[0].trim();
                if (StringUtils.hasText(firstAddress) && firstAddress.length() <= 128) {
                    return firstAddress;
                }
            }
        }
        String address = request.getRemoteAddr();
        return StringUtils.hasText(address) ? address : "unknown";
    }

    private String generateCode() {
        return "%06d".formatted(SECURE_RANDOM.nextInt(1_000_000));
    }

    private String randomToken() {
        byte[] bytes = new byte[RESET_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String codeDigest(String normalizedEmail, String code, String pepper) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(pepper.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(
                    mac.doFinal((normalizedEmail + ":" + code).getBytes(StandardCharsets.UTF_8))
            );
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 is unavailable", e);
        }
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
