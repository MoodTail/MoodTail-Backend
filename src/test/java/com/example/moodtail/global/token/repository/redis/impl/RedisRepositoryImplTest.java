package com.example.moodtail.global.token.repository.redis.impl;

import com.example.moodtail.support.auth.AuthPropertiesFixtures;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;

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
    void storesOAuthStateInsideConfiguredAuthNamespace() {
        repository.saveOAuthState(
                "state-value",
                "guest:7",
                7L,
                "KAKAO",
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_",
                Duration.ofMinutes(5)
        );

        verify(redisTemplate).execute(
                any(),
                eq(List.of(
                        "moodtail:auth:test:oauth-state-owner:kakao:guest:7",
                        "moodtail:auth:test:oauth-state:kakao:state-value"
                )),
                eq("moodtail:auth:test:oauth-state:kakao:"),
                anyString(),
                eq("300000"),
                eq("state-value")
        );
    }

    @Test
    void storesSocialSignupSessionInsideConfiguredAuthNamespace() {
        RedisRepository.SocialSignupSession session = new RedisRepository.SocialSignupSession(
                "GOOGLE",
                "google-user-id",
                "user@example.com",
                null
        );

        repository.saveSocialSignupToken(
                "signup-token",
                session,
                Duration.ofMinutes(10)
        );

        verify(valueOperations).set(
                eq("moodtail:auth:test:social-signup:signup-token"),
                anyString(),
                eq(Duration.ofMinutes(10))
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

    @Test
    void rateLimitAllowsTheConfiguredBoundaryAndRejectsTheNextRequest() {
        when(redisTemplate.execute(any(), any(), anyString()))
                .thenReturn(20L, 21L);

        assertThat(repository.acquireLocalAuthSlot(
                "login",
                "client-fingerprint",
                20,
                Duration.ofMinutes(1)
        )).isTrue();
        assertThat(repository.acquireLocalAuthSlot(
                "login",
                "client-fingerprint",
                20,
                Duration.ofMinutes(1)
        )).isFalse();
    }
}
