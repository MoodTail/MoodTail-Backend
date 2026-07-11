package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.config.AuthProperties;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static com.example.moodtail.domain.user.support.AuthPropertiesFixtures.defaults;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestLoginRateLimiterTest {

    private static final UUID GUEST_UUID = UUID.fromString("b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d");

    @Mock
    private RedisRepository redisRepository;

    @Test
    void checksConfiguredClientAddressAndGuestUuidWithIndependentLimits() {
        AuthProperties properties = withGuestLogin(new AuthProperties.GuestLogin(
                "X-Forwarded-For",
                "게스트",
                new AuthProperties.RateLimit(7, 30_000L),
                new AuthProperties.RateLimit(40, 120_000L)
        ));
        GuestLoginRateLimiter limiter = new GuestLoginRateLimiter(redisRepository, properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.10");
        request.addHeader("X-Forwarded-For", "203.0.113.7, 10.0.0.10");
        when(redisRepository.acquireGuestLoginSlot(anyString(), anyInt(), any())).thenReturn(true);

        limiter.check(GUEST_UUID, request);

        ArgumentCaptor<String> fingerprintCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> attemptsCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Duration> windowCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(redisRepository, org.mockito.Mockito.times(2)).acquireGuestLoginSlot(
                fingerprintCaptor.capture(),
                attemptsCaptor.capture(),
                windowCaptor.capture()
        );
        assertThat(fingerprintCaptor.getAllValues())
                .hasSize(2)
                .allSatisfy(fingerprint -> assertThat(fingerprint).matches("[0-9a-f]{64}"))
                .doesNotHaveDuplicates();
        assertThat(attemptsCaptor.getAllValues()).isEqualTo(List.of(40, 7));
        assertThat(windowCaptor.getAllValues()).isEqualTo(List.of(
                Duration.ofMinutes(2),
                Duration.ofSeconds(30)
        ));
    }

    @Test
    void stopsBeforeUuidCheckWhenClientLimitIsExceeded() {
        GuestLoginRateLimiter limiter = new GuestLoginRateLimiter(redisRepository, defaults());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.7");
        when(redisRepository.acquireGuestLoginSlot(anyString(), anyInt(), any())).thenReturn(false);

        assertThatThrownBy(() -> limiter.check(GUEST_UUID, request))
                .isInstanceOf(RestApiException.class);

        verify(redisRepository).acquireGuestLoginSlot(anyString(), anyInt(), any());
        verify(redisRepository, never()).acquireGuestLoginSlot(
                anyString(),
                org.mockito.ArgumentMatchers.eq(10),
                any()
        );
    }

    @Test
    void redisFailureFailsClosedWithServiceUnavailable() {
        GuestLoginRateLimiter limiter = new GuestLoginRateLimiter(redisRepository, defaults());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.7");
        when(redisRepository.acquireGuestLoginSlot(anyString(), anyInt(), any()))
                .thenThrow(new RedisConnectionFailureException("redis unavailable"));

        assertThatThrownBy(() -> limiter.check(GUEST_UUID, request))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH028")
                );
    }

    private AuthProperties withGuestLogin(AuthProperties.GuestLogin guestLogin) {
        AuthProperties properties = defaults();
        return new AuthProperties(
                properties.oauth(),
                properties.refreshCookie(),
                guestLogin,
                properties.concurrency(),
                properties.redis()
        );
    }
}
