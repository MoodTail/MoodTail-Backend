package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.model.ConsumedOAuthState;
import com.example.moodtail.domain.auth.model.OAuthState;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import com.example.moodtail.support.auth.AuthPropertiesFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthStateServiceTest {

    private static final String EXPIRED_STATE = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static final String ANONYMOUS_STATE = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
    private static final String MALFORMED_VERIFIER_STATE = "ccccccccccccccccccccccccccccccccccccccccccc";
    private static final String VALID_STATE = "ddddddddddddddddddddddddddddddddddddddddddd";
    private static final String VALID_VERIFIER =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_";

    @Mock
    private RedisRepository redisRepository;

    private OAuthStateService service;

    @BeforeEach
    void setUp() {
        service = new OAuthStateService(redisRepository, AuthPropertiesFixtures.defaults());
        lenient().when(redisRepository.acquireOAuthStateSlot(anyString(), anyString(), anyInt(), any()))
                .thenReturn(true);
    }

    @Test
    void issueStoresUnpredictableStateForGuestOwner() {
        OAuthState state = service.issue(2L, "203.0.113.7", SocialProvider.KAKAO);

        assertThat(state.value()).hasSize(43);
        assertThat(state.expiresInSeconds()).isEqualTo(300L);
        ArgumentCaptor<String> stateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> verifierCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisRepository).saveOAuthState(
                stateCaptor.capture(),
                eq("guest:2"),
                eq(2L),
                eq("KAKAO"),
                verifierCaptor.capture(),
                eq(Duration.ofMinutes(5))
        );
        assertThat(stateCaptor.getValue()).isEqualTo(state.value());
        assertThat(verifierCaptor.getValue()).matches("[A-Za-z0-9\\-._~]{43,128}");
        assertThat(state.codeChallenge()).isEqualTo(createChallenge(verifierCaptor.getValue()));
        assertThat(state.codeChallengeMethod()).isEqualTo("S256");
    }

    @Test
    void issueAllowsAnonymousOwnerWithoutGuestSession() {
        OAuthState state = service.issue(null, "203.0.113.7", SocialProvider.GOOGLE);

        ArgumentCaptor<String> stateOwnerCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisRepository).saveOAuthState(
                eq(state.value()),
                stateOwnerCaptor.capture(),
                eq(null),
                eq("GOOGLE"),
                anyString(),
                eq(Duration.ofMinutes(5))
        );
        assertThat(stateOwnerCaptor.getValue()).isEqualTo("anonymous-state:" + state.value());

        ArgumentCaptor<String> rateOwnerCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisRepository).acquireOAuthStateSlot(
                rateOwnerCaptor.capture(),
                eq("GOOGLE"),
                eq(10),
                eq(Duration.ofMinutes(1))
        );
        assertThat(rateOwnerCaptor.getValue())
                .startsWith("anonymous:")
                .hasSize("anonymous:".length() + 64);
    }

    @Test
    void consumeRejectsMissingOrReplayedState() {
        when(redisRepository.consumeOAuthStateSession(EXPIRED_STATE, "KAKAO"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consumeForAuthentication(EXPIRED_STATE, SocialProvider.KAKAO))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH018")
                );
    }

    @Test
    void consumeAllowsStateWithoutGuestOwner() {
        when(redisRepository.consumeOAuthStateSession(ANONYMOUS_STATE, "GOOGLE"))
                .thenReturn(Optional.of(new RedisRepository.OAuthStateSession(null, VALID_VERIFIER)));

        ConsumedOAuthState consumed = service.consumeForAuthentication(
                ANONYMOUS_STATE,
                SocialProvider.GOOGLE
        );

        assertThat(consumed.guestUserId()).isNull();
        assertThat(consumed.codeVerifier()).isEqualTo(VALID_VERIFIER);
    }

    @Test
    void consumeRejectsMalformedPersistedPkceVerifier() {
        when(redisRepository.consumeOAuthStateSession(MALFORMED_VERIFIER_STATE, "GOOGLE"))
                .thenReturn(Optional.of(new RedisRepository.OAuthStateSession(
                        2L,
                        "012345678901234567890123456789012345678901+"
                )));

        assertThatThrownBy(() -> service.consumeForAuthentication(
                MALFORMED_VERIFIER_STATE,
                SocialProvider.GOOGLE
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH018")
        );
    }

    @Test
    void consumeReturnsStoredGuestOwnerAndVerifier() {
        when(redisRepository.consumeOAuthStateSession(VALID_STATE, "GOOGLE"))
                .thenReturn(Optional.of(new RedisRepository.OAuthStateSession(2L, VALID_VERIFIER)));

        ConsumedOAuthState consumed = service.consumeForAuthentication(VALID_STATE, SocialProvider.GOOGLE);

        assertThat(consumed.guestUserId()).isEqualTo(2L);
        assertThat(consumed.codeVerifier()).isEqualTo(VALID_VERIFIER);
    }

    @Test
    void issueReturnsServiceUnavailableWhenRedisCannotStoreState() {
        org.mockito.Mockito.doThrow(new RedisConnectionFailureException("redis unavailable"))
                .when(redisRepository)
                .saveOAuthState(anyString(), eq("guest:2"), eq(2L), eq("GOOGLE"), anyString(), any());

        assertThatThrownBy(() -> service.issue(2L, "203.0.113.7", SocialProvider.GOOGLE))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH028")
                );
    }

    @Test
    void issueRejectsRequestsBeyondConfiguredRateLimit() {
        when(redisRepository.acquireOAuthStateSlot(
                "guest:2",
                "GOOGLE",
                10,
                Duration.ofMinutes(1)
        )).thenReturn(false);

        assertThatThrownBy(() -> service.issue(2L, "203.0.113.7", SocialProvider.GOOGLE))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH031")
                );
    }

    private String createChallenge(String verifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
