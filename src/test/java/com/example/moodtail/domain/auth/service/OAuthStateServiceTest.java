package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.model.ConsumedOAuthState;
import com.example.moodtail.domain.auth.model.OAuthState;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.support.auth.AuthPropertiesFixtures;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class OAuthStateServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisRepository redisRepository;

    private OAuthStateService service;

    @BeforeEach
    void setUp() {
        service = new OAuthStateService(userRepository, redisRepository, AuthPropertiesFixtures.defaults());
        lenient().when(redisRepository.acquireOAuthStateSlot(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(true);
    }

    @Test
    void issueStoresUnpredictableStateWithGuestAndProvider() {
        User guest = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );
        when(userRepository.findById(2L)).thenReturn(Optional.of(guest));

        OAuthState state = service.issue(2L, SocialProvider.KAKAO);

        assertThat(state.value()).hasSizeGreaterThanOrEqualTo(40);
        assertThat(state.expiresInSeconds()).isEqualTo(300L);
        ArgumentCaptor<String> stateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> verifierCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisRepository).saveOAuthState(
                stateCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.eq("KAKAO"),
                verifierCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(Duration.ofMinutes(5))
        );
        assertThat(stateCaptor.getValue()).isEqualTo(state.value());
        assertThat(verifierCaptor.getValue()).matches("[A-Za-z0-9\\-._~]{43,128}");
        assertThat(state.codeChallenge()).isEqualTo(createChallenge(verifierCaptor.getValue()));
        assertThat(state.codeChallengeMethod()).isEqualTo("S256");
    }

    @Test
    void issueRejectsMissingGuestIdBeforeRepositoryAccess() {
        assertThatThrownBy(() -> service.issue(null, SocialProvider.KAKAO))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH019")
                );
    }

    @Test
    void consumeRejectsMissingOrReplayedState() {
        when(redisRepository.consumeOAuthStateSession("expired-state", "KAKAO"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consumeForAuthentication("expired-state", SocialProvider.KAKAO))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH018")
                );
    }

    @Test
    void consumeRejectsBlankStateBeforeRedisAccess() {
        assertThatThrownBy(() -> service.consumeForAuthentication(" ", SocialProvider.KAKAO))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH018")
                );
    }

    @Test
    void consumeRejectsStateWithoutGuestOwner() {
        when(redisRepository.consumeOAuthStateSession("ownerless-state", "GOOGLE"))
                .thenReturn(Optional.of(new RedisRepository.OAuthStateSession(
                        null,
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_"
                )));

        assertThatThrownBy(() -> service.consumeForAuthentication("ownerless-state", SocialProvider.GOOGLE))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH018")
                );
    }

    @Test
    void consumeRejectsMalformedPersistedPkceVerifier() {
        User guest = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );
        when(redisRepository.consumeOAuthStateSession("malformed-state", "GOOGLE"))
                .thenReturn(Optional.of(new RedisRepository.OAuthStateSession(
                        2L,
                        "012345678901234567890123456789012345678901+"
                )));
        when(userRepository.findById(2L)).thenReturn(Optional.of(guest));

        assertThatThrownBy(() -> service.consumeForAuthentication("malformed-state", SocialProvider.GOOGLE))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH018")
                );
    }

    @Test
    void consumeReturnsStateOwnerOnlyWhileGuestSessionIsStillValid() {
        User guest = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );
        String verifier = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_";
        when(redisRepository.consumeOAuthStateSession("valid-state", "GOOGLE"))
                .thenReturn(Optional.of(new RedisRepository.OAuthStateSession(
                        2L,
                        verifier
                )));
        when(userRepository.findById(2L)).thenReturn(Optional.of(guest));

        ConsumedOAuthState consumed =
                service.consumeForAuthentication("valid-state", SocialProvider.GOOGLE);
        assertThat(consumed.guestUserId()).isEqualTo(2L);
        assertThat(consumed.codeVerifier()).isEqualTo(verifier);
    }

    @Test
    void consumeRejectsStateAfterGuestWasAlreadyUpgraded() {
        User upgradedUser = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );
        upgradedUser.upgradeToUser("회원", LocalDateTime.now());
        when(redisRepository.consumeOAuthStateSession("stale-state", "KAKAO"))
                .thenReturn(Optional.of(new RedisRepository.OAuthStateSession(
                        2L,
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_"
                )));
        when(userRepository.findById(2L)).thenReturn(Optional.of(upgradedUser));

        assertThatThrownBy(() -> service.consumeForAuthentication("stale-state", SocialProvider.KAKAO))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH019")
                );
    }

    @Test
    void issueRejectsSoftDeletedGuest() {
        User guest = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );
        guest.delete();
        when(userRepository.findById(2L)).thenReturn(Optional.of(guest));

        assertThatThrownBy(() -> service.issue(2L, SocialProvider.KAKAO))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH019")
                );
    }

    @Test
    void issueReturnsServiceUnavailableWhenRedisCannotStoreState() {
        User guest = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );
        when(userRepository.findById(2L)).thenReturn(Optional.of(guest));
        org.mockito.Mockito.doThrow(new RedisConnectionFailureException("redis unavailable"))
                .when(redisRepository).saveOAuthState(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.eq(2L),
                        org.mockito.ArgumentMatchers.eq("GOOGLE"),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any()
                );

        assertThatThrownBy(() -> service.issue(2L, SocialProvider.GOOGLE))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH028")
                );
    }

    @Test
    void issueRejectsRequestsBeyondConfiguredRateLimit() {
        User guest = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );
        when(userRepository.findById(2L)).thenReturn(Optional.of(guest));
        when(redisRepository.acquireOAuthStateSlot(2L, "GOOGLE", 10, Duration.ofMinutes(1)))
                .thenReturn(false);

        assertThatThrownBy(() -> service.issue(2L, SocialProvider.GOOGLE))
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
