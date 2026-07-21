package com.example.moodtail.domain.auth.validator;

import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalAuthRateLimiterTest {

    @Mock
    private RedisRepository redisRepository;

    @Test
    void appliesFixedPurposeSpecificLimitsToHashedClientAddress() {
        LocalAuthRateLimiter limiter = new LocalAuthRateLimiter(redisRepository);
        when(redisRepository.acquireLocalAuthSlot(anyString(), anyString(), anyInt(), any()))
                .thenReturn(true);

        limiter.checkLogin("203.0.113.7");
        limiter.checkSignup("203.0.113.7");
        limiter.checkEmailAvailability("203.0.113.7");

        ArgumentCaptor<String> purpose = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fingerprint = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> attempts = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Duration> window = ArgumentCaptor.forClass(Duration.class);
        verify(redisRepository, org.mockito.Mockito.times(3)).acquireLocalAuthSlot(
                purpose.capture(),
                fingerprint.capture(),
                attempts.capture(),
                window.capture()
        );
        assertThat(purpose.getAllValues()).containsExactly("login", "signup", "email-availability");
        assertThat(fingerprint.getAllValues())
                .allMatch(value -> value.matches("[0-9a-f]{64}"))
                .containsOnly(fingerprint.getAllValues().get(0));
        assertThat(attempts.getAllValues()).containsExactly(20, 5, 30);
        assertThat(window.getAllValues()).containsExactly(
                Duration.ofMinutes(1),
                Duration.ofMinutes(10),
                Duration.ofMinutes(1)
        );
    }

    @Test
    void rejectsARequestWhenThePurposeLimitIsExhausted() {
        LocalAuthRateLimiter limiter = new LocalAuthRateLimiter(redisRepository);
        when(redisRepository.acquireLocalAuthSlot(anyString(), anyString(), anyInt(), any()))
                .thenReturn(false);

        assertThatThrownBy(() -> limiter.checkSignup("203.0.113.7"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH043")
                );
    }

    @Test
    void failsClosedWhenRedisIsUnavailable() {
        LocalAuthRateLimiter limiter = new LocalAuthRateLimiter(redisRepository);
        when(redisRepository.acquireLocalAuthSlot(anyString(), anyString(), anyInt(), any()))
                .thenThrow(new RedisConnectionFailureException("redis unavailable"));

        assertThatThrownBy(() -> limiter.checkLogin("203.0.113.7"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH028")
                );
    }

    @Test
    void appliesRateLimitToSharedUnknownBucketWhenClientAddressIsMissing() {
        LocalAuthRateLimiter limiter = new LocalAuthRateLimiter(redisRepository);
        when(redisRepository.acquireLocalAuthSlot(anyString(), anyString(), anyInt(), any()))
                .thenReturn(true);

        limiter.checkLogin(null);
        limiter.checkSignup(" ");

        ArgumentCaptor<String> fingerprint = ArgumentCaptor.forClass(String.class);
        verify(redisRepository, org.mockito.Mockito.times(2)).acquireLocalAuthSlot(
                anyString(),
                fingerprint.capture(),
                anyInt(),
                any()
        );
        assertThat(fingerprint.getAllValues())
                .allMatch(value -> value.matches("[0-9a-f]{64}"))
                .containsOnly(fingerprint.getAllValues().get(0));
    }
}
