package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.model.SocialSignupSession;
import com.example.moodtail.domain.auth.model.SocialSignupTicket;
import com.example.moodtail.global.auth.config.AuthProperties;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.auth.model.SocialUserProfile;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.required;

@Service
@RequiredArgsConstructor
public class SocialSignupSessionService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;
    private static final String TOKEN_PATTERN = "^[A-Za-z0-9_-]{43}$";

    private final RedisRepository redisRepository;
    private final AuthProperties authProperties;

    public SocialSignupTicket issue(SocialUserProfile profile) {
        String token = generateToken();
        Duration ttl = Duration.ofMillis(authProperties.oauth().signupTokenExpirationMillis());
        required(
                "save social signup session",
                () -> redisRepository.saveSocialSignupToken(
                        token,
                        new RedisRepository.SocialSignupSession(
                                profile.provider().name(),
                                profile.providerUserId(),
                                profile.email()
                        ),
                        ttl
                )
        );
        return new SocialSignupTicket(token, ttl.toSeconds());
    }

    public SocialSignupSession consume(String token) {
        if (!StringUtils.hasText(token) || !token.matches(TOKEN_PATTERN)) {
            throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_SIGNUP_TOKEN);
        }
        RedisRepository.SocialSignupSession storedSession = required(
                "consume social signup session",
                () -> redisRepository.consumeSocialSignupToken(token)
        ).orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_SOCIAL_SIGNUP_TOKEN));

        try {
            if (!StringUtils.hasText(storedSession.provider())) {
                throw new IllegalArgumentException("Invalid social signup provider");
            }
            SocialProvider provider = SocialProvider.valueOf(storedSession.provider());
            if (!StringUtils.hasText(storedSession.providerUserId())
                    || !StringUtils.hasText(storedSession.email())) {
                throw new IllegalArgumentException("Invalid social signup session");
            }
            return new SocialSignupSession(
                    provider,
                    storedSession.providerUserId(),
                    storedSession.email()
            );
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_SIGNUP_TOKEN);
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
