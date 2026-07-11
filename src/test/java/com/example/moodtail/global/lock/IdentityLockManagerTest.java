package com.example.moodtail.global.lock;

import com.example.moodtail.domain.user.enums.SocialProvider;
import com.example.moodtail.domain.user.support.AuthPropertiesFixtures;
import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityLockManagerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void releaseFailureDoesNotReplaceSuccessfulApplicationResult() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                anyList(),
                any(Object[].class)
        )).thenThrow(new RedisConnectionFailureException("redis unavailable"));
        IdentityLockManager manager = new IdentityLockManager(
                redisTemplate,
                AuthPropertiesFixtures.defaults()
        );

        String result = manager.executeForSocialLogin(
                SocialProvider.GOOGLE,
                "provider-user-id",
                () -> "success"
        );

        assertThat(result).isEqualTo("success");
    }

    @Test
    void acquireFailureIsReportedAsServiceUnavailable() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class)))
                .thenThrow(new RedisConnectionFailureException("redis unavailable"));
        IdentityLockManager manager = new IdentityLockManager(
                redisTemplate,
                AuthPropertiesFixtures.defaults()
        );

        assertThatThrownBy(() -> manager.executeForGuestUser("guest-uuid", () -> "unused"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH028")
                );
    }
}
