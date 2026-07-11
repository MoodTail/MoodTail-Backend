package com.example.moodtail.global.token.redis;

import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthRedisFailurePolicyTest {

    @Test
    void requiredOperationMapsRedisFailureToServiceUnavailable() {
        assertThatThrownBy(() -> AuthRedisFailurePolicy.required(
                "test required operation",
                () -> {
                    throw new RedisConnectionFailureException("redis unavailable");
                }
        )).isInstanceOfSatisfying(RestApiException.class, exception -> {
            org.assertj.core.api.Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH028");
            org.assertj.core.api.Assertions.assertThat(exception.getErrorCode().getHttpStatus().value()).isEqualTo(503);
        });
    }

    @Test
    void bestEffortOperationDoesNotReplaceSuccessfulApplicationResult() {
        assertThatCode(() -> AuthRedisFailurePolicy.bestEffort(
                "test best-effort operation",
                () -> {
                    throw new RedisConnectionFailureException("redis unavailable");
                }
        )).doesNotThrowAnyException();
    }
}
