package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.model.ConsumedOAuthState;
import com.example.moodtail.domain.auth.model.OAuthState;
import com.example.moodtail.global.auth.config.AuthProperties;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.auth.validator.PkceCodeVerifierValidator;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.required;

@Service
@RequiredArgsConstructor
public class OAuthStateService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int STATE_BYTE_LENGTH = 32;
    private static final int CODE_VERIFIER_BYTE_LENGTH = 32;
    private static final String CODE_CHALLENGE_METHOD = "S256";
    private static final String STATE_PATTERN = "^[A-Za-z0-9_-]{43}$";

    private final RedisRepository redisRepository;
    private final AuthProperties authProperties;

    public OAuthState issue(Long guestUserId, SocialProvider provider) {
        if (guestUserId == null || provider == null) {
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

    public ConsumedOAuthState consumeForAuthentication(String state, SocialProvider provider) {
        if (!StringUtils.hasText(state) || !state.matches(STATE_PATTERN) || provider == null) {
            throw new RestApiException(AuthErrorStatus.INVALID_OAUTH_STATE);
        }
        RedisRepository.OAuthStateSession session = required(
                "consume OAuth state",
                () -> redisRepository.consumeOAuthStateSession(state, provider.name())
        )
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_OAUTH_STATE));
        Long guestUserId = session.guestUserId();
        if (guestUserId == null) {
            throw new RestApiException(AuthErrorStatus.INVALID_OAUTH_STATE);
        }
        if (!PkceCodeVerifierValidator.isValid(session.codeVerifier())) {
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
                    .digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

}
