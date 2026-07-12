package com.example.moodtail.global.token.redis;

import com.example.moodtail.global.common.exception.RestApiException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;

import java.util.function.Supplier;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthRedisFailurePolicy {

    public static <T> T required(String operation, Supplier<T> action) {
        try {
            return action.get();
        } catch (DataAccessException e) {
            log.error("Required Redis operation failed: {} ({})", operation, e.getClass().getSimpleName());
            log.debug("Required Redis failure details", e);
            throw new RestApiException(AUTH_INFRASTRUCTURE_UNAVAILABLE);
        }
    }

    public static void required(String operation, Runnable action) {
        required(operation, () -> {
            action.run();
            return null;
        });
    }

    public static void bestEffort(String operation, Runnable action) {
        try {
            action.run();
        } catch (DataAccessException e) {
            log.warn("Non-critical Redis operation failed: {} ({})", operation, e.getClass().getSimpleName());
            log.debug("Non-critical Redis failure details", e);
        }
    }
}
