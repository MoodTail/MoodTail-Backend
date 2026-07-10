package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.config.AuthProperties;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.enums.SocialProvider;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthStateService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int STATE_BYTE_LENGTH = 32;

    private final UserRepository userRepository;
    private final RedisRepository redisRepository;
    private final AuthProperties authProperties;

    public OAuthState issue(Long guestUserId, SocialProvider provider) {
        User guestUser = userRepository.findById(guestUserId)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION));
        if (!guestUser.isGuest() || !guestUser.isActive()) {
            throw new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION);
        }

        String state = generateState();
        Duration ttl = Duration.ofMillis(authProperties.oauth().stateExpirationMillis());
        redisRepository.saveOAuthState(state, guestUserId, provider.name(), ttl);
        return new OAuthState(state, ttl.toSeconds());
    }

    public Long consume(String state, SocialProvider provider) {
        return redisRepository.consumeOAuthState(state, provider.name())
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_OAUTH_STATE));
    }

    private String generateState() {
        byte[] bytes = new byte[STATE_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record OAuthState(String value, long expiresInSeconds) {
    }
}
