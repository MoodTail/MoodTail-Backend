package com.example.moodtail.global.lock;

import com.example.moodtail.global.auth.config.AuthProperties;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

import static com.example.moodtail.global.common.util.Sha256Hasher.hashToHex;
import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.bestEffort;
import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.required;

@Component
@RequiredArgsConstructor
public class IdentityLockManager {

    private static final String LOCK_KEY_PREFIX = "lock:identity:";
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    private final RedisTemplate<String, String> redisTemplate;
    private final AuthProperties authProperties;

    public <T> T executeForSocialLogin(
            SocialProvider provider,
            String providerUserId,
            Supplier<T> action
    ) {
        return executeWithLock(
                "social-login:" + provider.name().toLowerCase(Locale.ROOT) + ":" + providerUserId,
                action
        );
    }

    public <T> T executeForGuestUser(String guestUuid, Supplier<T> action) {
        return executeWithLock(
                "guest-user:" + guestUuid,
                action
        );
    }

    public <T> T executeForGuestUserId(Long guestUserId, Supplier<T> action) {
        return executeWithLock(
                "guest-user-id:" + guestUserId,
                action
        );
    }

    public <T> T executeForLocalEmail(String normalizedEmail, Supplier<T> action) {
        return executeWithLock(
                "local-email:" + normalizedEmail,
                action
        );
    }

    public <T> T executeForPasswordResetToken(String resetToken, Supplier<T> action) {
        return executeWithLock(
                "password-reset-token:" + resetToken,
                action
        );
    }

    private <T> T executeWithLock(String identifier, Supplier<T> action) {
        String key = authProperties.redis().keyPrefix() + LOCK_KEY_PREFIX + hashToHex(identifier);
        String owner = UUID.randomUUID().toString();
        acquire(key, owner);

        try {
            return action.get();
        } finally {
            bestEffort("release identity lock", () -> release(key, owner));
        }
    }

    private void acquire(String key, String owner) {
        AuthProperties.Concurrency concurrency = authProperties.concurrency();
        long deadline = System.nanoTime() + Duration.ofMillis(concurrency.acquireTimeoutMillis()).toNanos();

        while (System.nanoTime() < deadline) {
            Boolean acquired = required(
                    "acquire identity lock",
                    () -> redisTemplate.opsForValue().setIfAbsent(
                            key,
                            owner,
                            Duration.ofMillis(concurrency.lockTtlMillis())
                    )
            );
            if (Boolean.TRUE.equals(acquired)) {
                return;
            }
            LockSupport.parkNanos(Duration.ofMillis(concurrency.retryIntervalMillis()).toNanos());
        }

        throw new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE);
    }

    private void release(String key, String owner) {
        redisTemplate.execute(RELEASE_LOCK_SCRIPT, List.of(key), owner);
    }

}
