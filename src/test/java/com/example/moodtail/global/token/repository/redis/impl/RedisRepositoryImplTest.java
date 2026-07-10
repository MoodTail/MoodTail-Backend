package com.example.moodtail.global.token.repository.redis.impl;

import com.example.moodtail.domain.user.support.AuthPropertiesFixtures;
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
import java.util.concurrent.TimeUnit;

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
        repository = new RedisRepositoryImpl(redisTemplate, AuthPropertiesFixtures.defaults());
        ReflectionTestUtils.setField(repository, "jwtRefreshExpirationMillis", 1_209_600_000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
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
        repository.saveOAuthState("state-value", 7L, "KAKAO", Duration.ofMinutes(5));

        verify(valueOperations).set(
                "moodtail:auth:test:oauth-state:state-value",
                "KAKAO:7",
                Duration.ofMinutes(5)
        );
    }

    @Test
    void storesAccessBlacklistInsideConfiguredAuthNamespace() {
        Claims claims = Jwts.claims().setId("access-jti");
        claims.setExpiration(new Date(System.currentTimeMillis() + 60_000L));

        repository.blockAccessToken("access-token", claims);

        verify(valueOperations).set(
                org.mockito.ArgumentMatchers.eq("moodtail:auth:test:access-blacklist:access-jti"),
                org.mockito.ArgumentMatchers.eq("blacklisted"),
                org.mockito.ArgumentMatchers.longThat(ttl -> ttl > 0 && ttl <= 60_000L),
                org.mockito.ArgumentMatchers.eq(TimeUnit.MILLISECONDS)
        );
    }
}
