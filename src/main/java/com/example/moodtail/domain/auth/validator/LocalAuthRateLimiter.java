package com.example.moodtail.domain.auth.validator;

import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.required;

@Component
@RequiredArgsConstructor
public class LocalAuthRateLimiter {

    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(1);
    private static final Duration SIGNUP_WINDOW = Duration.ofMinutes(10);
    private static final Duration EMAIL_AVAILABILITY_WINDOW = Duration.ofMinutes(1);
    private static final int LOGIN_MAX_ATTEMPTS = 20;
    private static final int SIGNUP_MAX_ATTEMPTS = 5;
    private static final int EMAIL_AVAILABILITY_MAX_ATTEMPTS = 30;

    private final RedisRepository redisRepository;

    public void checkLogin(String clientAddress) {
        check("login", clientAddress, LOGIN_MAX_ATTEMPTS, LOGIN_WINDOW);
    }

    public void checkSignup(String clientAddress) {
        check("signup", clientAddress, SIGNUP_MAX_ATTEMPTS, SIGNUP_WINDOW);
    }

    public void checkEmailAvailability(String clientAddress) {
        check(
                "email-availability",
                clientAddress,
                EMAIL_AVAILABILITY_MAX_ATTEMPTS,
                EMAIL_AVAILABILITY_WINDOW
        );
    }

    private void check(String purpose, String clientAddress, int maxAttempts, Duration window) {
        boolean allowed = required(
                "acquire local-auth rate-limit slot",
                () -> redisRepository.acquireLocalAuthSlot(
                        purpose,
                        sha256(clientAddress),
                        maxAttempts,
                        window
                )
        );
        if (!allowed) {
            throw new RestApiException(AuthErrorStatus.TOO_MANY_LOCAL_AUTH_REQUESTS);
        }
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
