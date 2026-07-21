package com.example.moodtail.domain.auth.validator;

import com.example.moodtail.global.auth.config.AuthProperties;
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
import java.util.UUID;

import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.required;

@Component
@RequiredArgsConstructor
public class GuestLoginRateLimiter {

    private final RedisRepository redisRepository;
    private final AuthProperties authProperties;

    public void check(UUID guestUuid, String clientAddress) {
        checkLimit("client", clientAddress, authProperties.guestLogin().clientRateLimit());
        checkLimit("uuid", guestUuid.toString(), authProperties.guestLogin().uuidRateLimit());
    }

    private void checkLimit(String scope, String identifier, AuthProperties.RateLimit rateLimit) {
        if (identifier == null || identifier.isBlank()) {
            return;
        }
        boolean acquired = required(
                "acquire guest login rate-limit slot",
                () -> redisRepository.acquireGuestLoginSlot(
                        hash(scope + ":" + identifier),
                        rateLimit.maxAttempts(),
                        Duration.ofMillis(rateLimit.windowMillis())
                )
        );
        if (!acquired) {
            throw new RestApiException(AuthErrorStatus.TOO_MANY_GUEST_LOGIN_REQUESTS);
        }
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
