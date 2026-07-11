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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;

import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.required;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthStateService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int STATE_BYTE_LENGTH = 32;
    private static final int CODE_VERIFIER_BYTE_LENGTH = 32;
    private static final String CODE_CHALLENGE_METHOD = "S256";

    private final UserRepository userRepository;
    private final RedisRepository redisRepository;
    private final AuthProperties authProperties;

    public OAuthState issue(Long guestUserId, SocialProvider provider) {
        User guestUser = userRepository.findById(guestUserId)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION));
        if (!guestUser.isGuest() || !guestUser.isActive() || guestUser.isDeleted()) {
            throw new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION);
        }

        AuthProperties.RateLimit rateLimit = authProperties.oauth().stateRateLimit();
        boolean acquired = required(
                "acquire OAuth state rate-limit slot",
                () -> redisRepository.acquireOAuthStateSlot(
                        guestUserId,
                        provider.name(),
                        rateLimit.maxAttempts(),
                        Duration.ofMillis(rateLimit.windowMillis())
                )
        );
        if (!acquired) {
            throw new RestApiException(AuthErrorStatus.TOO_MANY_OAUTH_STATE_REQUESTS);
        }

        String state = generateState();
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = createCodeChallenge(codeVerifier);
        Duration ttl = Duration.ofMillis(authProperties.oauth().stateExpirationMillis());
        required(
                "save OAuth state",
                () -> redisRepository.saveOAuthState(
                        state,
                        guestUserId,
                        provider.name(),
                        codeVerifier,
                        ttl
                )
        );
        return new OAuthState(state, codeChallenge, CODE_CHALLENGE_METHOD, ttl.toSeconds());
    }

    public Long consume(String state, SocialProvider provider) {
        return consumeForAuthentication(state, provider).guestUserId();
    }

    public ConsumedOAuthState consumeForAuthentication(String state, SocialProvider provider) {
        RedisRepository.OAuthStateSession session = required(
                "consume OAuth state",
                () -> redisRepository.consumeOAuthStateSession(state, provider.name())
        )
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_OAUTH_STATE));
        Long guestUserId = session.guestUserId();
        User guestUser = userRepository.findById(guestUserId)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION));
        if (!guestUser.isGuest() || !guestUser.isActive() || guestUser.isDeleted()) {
            throw new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION);
        }
        if (session.codeVerifier() == null || session.codeVerifier().length() < 43) {
            throw new RestApiException(AuthErrorStatus.INVALID_OAUTH_STATE);
        }
        return new ConsumedOAuthState(guestUserId, session.codeVerifier());
    }

    private String generateState() {
        byte[] bytes = new byte[STATE_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateCodeVerifier() {
        byte[] bytes = new byte[CODE_VERIFIER_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String createCodeChallenge(String codeVerifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(codeVerifier.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public record OAuthState(
            String value,
            String codeChallenge,
            String codeChallengeMethod,
            long expiresInSeconds
    ) {
        public OAuthState(String value, long expiresInSeconds) {
            this(value, null, null, expiresInSeconds);
        }
    }

    public record ConsumedOAuthState(Long guestUserId, String codeVerifier) {
    }
}
