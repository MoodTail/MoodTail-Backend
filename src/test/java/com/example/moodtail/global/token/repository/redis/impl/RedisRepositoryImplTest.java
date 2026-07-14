package com.example.moodtail.global.token.repository.redis.impl;

import com.example.moodtail.support.auth.AuthPropertiesFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisRepositoryImplTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new RedisRepositoryImpl(
                redisTemplate,
                AuthPropertiesFixtures.defaults(),
                new ObjectMapper()
        );
        ReflectionTestUtils.setField(repository, "jwtRefreshExpirationMillis", 1_209_600_000L);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void storesRefreshJtiInsideConfiguredAuthNamespace() {
        repository.saveRefreshJti(42L, "refresh-jti");

        verify(valueOperations).set(
                "moodtail:auth:test:refresh:42",
                "refresh-jti",
                Duration.ofDays(14)
        );
    }

    @Test
    void restoresRefreshJtiOnlyWhenNoNewerSessionExists() {
        when(valueOperations.setIfAbsent(
                "moodtail:auth:test:refresh:42",
                "previous-jti",
                Duration.ofDays(14)
        )).thenReturn(true);

        boolean restored = repository.saveRefreshJtiIfAbsent(42L, "previous-jti");

        assertThat(restored).isTrue();
    }

    @Test
    void deletesRefreshJtiOnlyWhenExpectedSessionStillOwnsTheKey() {
        when(redisTemplate.execute(
                any(),
                eq(List.of("moodtail:auth:test:refresh:42")),
                eq("issued-jti")
        )).thenReturn(1L);

        boolean deleted = repository.deleteRefreshJtiIfMatches(42L, "issued-jti");

        assertThat(deleted).isTrue();
    }

    @Test
    void storesOAuthStateInsideConfiguredAuthNamespace() {
        repository.saveOAuthState(
                "state-value",
                7L,
                "KAKAO",
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_",
                Duration.ofMinutes(5)
        );

        verify(redisTemplate).execute(
                any(),
                eq(List.of(
                        "moodtail:auth:test:oauth-state-owner:kakao:7",
                        "moodtail:auth:test:oauth-state:kakao:state-value"
                )),
                eq("moodtail:auth:test:oauth-state:kakao:"),
                anyString(),
                eq("300000"),
                eq("state-value")
        );
    }

    @Test
    void storesAccessBlacklistInsideConfiguredAuthNamespace() {
        Claims claims = Jwts.claims().setId("access-jti");
        claims.setExpiration(new Date(System.currentTimeMillis() + 60_000L));

        repository.blockAccessToken(claims);

        verify(valueOperations).set(
                org.mockito.ArgumentMatchers.eq("moodtail:auth:test:access-blacklist:access-jti"),
                org.mockito.ArgumentMatchers.eq("blacklisted"),
                org.mockito.ArgumentMatchers.longThat(ttl -> ttl > 0 && ttl <= 60_000L),
                org.mockito.ArgumentMatchers.eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    void storesPasswordResetCodeWithoutEmailOrRawCodeInKey() {
        repository.savePasswordResetCode(
                "email-fingerprint",
                3L,
                2,
                "code-digest",
                Duration.ofMinutes(5)
        );

        verify(valueOperations).set(
                "moodtail:auth:test:password-reset-code:email-fingerprint",
                "3:2:code-digest:0",
                Duration.ofMinutes(5)
        );
    }

    @Test
    void passwordResetCooldownUsesAtomicSetIfAbsent() {
        when(valueOperations.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(true);

        boolean acquired = repository.acquirePasswordResetCooldown(
                "email-fingerprint",
                Duration.ofMinutes(1)
        );

        assertThat(acquired).isTrue();
        verify(valueOperations).setIfAbsent(
                "moodtail:auth:test:password-reset-cooldown:email-fingerprint",
                "1",
                Duration.ofMinutes(1)
        );
    }
}
