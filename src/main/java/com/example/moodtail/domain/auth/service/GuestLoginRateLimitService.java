package com.example.moodtail.domain.auth.service;

import com.example.moodtail.global.auth.config.AuthProperties;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.UUID;

import static com.example.moodtail.global.common.util.Sha256Hasher.hashToHex;
import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.required;

@Service
@RequiredArgsConstructor
public class GuestLoginRateLimitService {

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
                        hashToHex(scope + ":" + identifier),
                        rateLimit.maxAttempts(),
                        Duration.ofMillis(rateLimit.windowMillis())
                )
        );
        if (!acquired) {
            throw new RestApiException(AuthErrorStatus.TOO_MANY_GUEST_LOGIN_REQUESTS);
        }
    }
}
