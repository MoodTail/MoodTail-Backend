package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.config.AuthProperties;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

    private static final int MAX_CLIENT_ADDRESS_LENGTH = 128;

    private final RedisRepository redisRepository;
    private final AuthProperties authProperties;

    public void check(UUID guestUuid, HttpServletRequest request) {
        checkLimit("client", resolveClientAddress(request), authProperties.guestLogin().clientRateLimit());
        checkLimit("uuid", guestUuid.toString(), authProperties.guestLogin().uuidRateLimit());
    }

    private String resolveClientAddress(HttpServletRequest request) {
        String configuredHeader = authProperties.guestLogin().clientIpHeader();
        if (StringUtils.hasText(configuredHeader)) {
            String forwardedAddress = request.getHeader(configuredHeader);
            if (StringUtils.hasText(forwardedAddress)) {
                String firstAddress = forwardedAddress.split(",", 2)[0].trim();
                if (StringUtils.hasText(firstAddress) && firstAddress.length() <= MAX_CLIENT_ADDRESS_LENGTH) {
                    return firstAddress;
                }
            }
        }
        return request.getRemoteAddr();
    }

    private void checkLimit(String scope, String identifier, AuthProperties.RateLimit rateLimit) {
        if (!StringUtils.hasText(identifier)) {
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
