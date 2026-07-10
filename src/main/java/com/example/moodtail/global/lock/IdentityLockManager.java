package com.example.moodtail.global.lock;

import com.example.moodtail.domain.user.config.AuthProperties;
import com.example.moodtail.domain.user.enums.SocialProvider;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.BaseCodeInterface;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.common.exception.code.status.GlobalErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

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
                "social-login:" + provider.name().toLowerCase() + ":" + providerUserId,
                action,
                AuthErrorStatus.FAILED_SOCIAL_LOGIN
        );
    }

    public <T> T executeForGuestUser(String guestUuid, Supplier<T> action) {
        return executeWithLock(
                "guest-user:" + guestUuid,
                action,
                GlobalErrorStatus._INTERNAL_SERVER_ERROR
        );
    }

    public <T> T executeForGuestUserId(Long guestUserId, Supplier<T> action) {
        return executeWithLock(
                "guest-user-id:" + guestUserId,
                action,
                AuthErrorStatus.INVALID_GUEST_SESSION
        );
    }

    private <T> T executeWithLock(String identifier, Supplier<T> action, BaseCodeInterface failureStatus) {
        String key = authProperties.redis().keyPrefix() + LOCK_KEY_PREFIX + hash(identifier);
        String owner = UUID.randomUUID().toString();
        acquire(key, owner, failureStatus);

        try {
            return action.get();
        } finally {
            release(key, owner);
        }
    }

    private void acquire(String key, String owner, BaseCodeInterface failureStatus) {
        AuthProperties.Concurrency concurrency = authProperties.concurrency();
        long deadline = System.nanoTime() + Duration.ofMillis(concurrency.acquireTimeoutMillis()).toNanos();

        while (System.nanoTime() < deadline) {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                    key,
                    owner,
                    Duration.ofMillis(concurrency.lockTtlMillis())
            );
            if (Boolean.TRUE.equals(acquired)) {
                return;
            }
            LockSupport.parkNanos(Duration.ofMillis(concurrency.retryIntervalMillis()).toNanos());
        }

        throw new RestApiException(failureStatus);
    }

    private void release(String key, String owner) {
        redisTemplate.execute(RELEASE_LOCK_SCRIPT, List.of(key), owner);
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
