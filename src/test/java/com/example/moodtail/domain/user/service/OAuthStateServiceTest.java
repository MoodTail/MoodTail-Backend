package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.enums.SocialProvider;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.domain.user.support.AuthPropertiesFixtures;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    }

    @Test
    void issueStoresUnpredictableStateWithGuestAndProvider() {
        User guest = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );
        when(userRepository.findById(2L)).thenReturn(Optional.of(guest));

        OAuthStateService.OAuthState state = service.issue(2L, SocialProvider.KAKAO);

        assertThat(state.value()).hasSizeGreaterThanOrEqualTo(40);
        assertThat(state.expiresInSeconds()).isEqualTo(300L);
        ArgumentCaptor<String> stateCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisRepository).saveOAuthState(
                stateCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.eq("KAKAO"),
                org.mockito.ArgumentMatchers.eq(Duration.ofMinutes(5))
        );
        assertThat(stateCaptor.getValue()).isEqualTo(state.value());
    }

    @Test
    void consumeRejectsMissingOrReplayedState() {
        when(redisRepository.consumeOAuthState("expired-state", "KAKAO"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consume("expired-state", SocialProvider.KAKAO))
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
        when(redisRepository.consumeOAuthState("valid-state", "GOOGLE")).thenReturn(Optional.of(2L));
        when(userRepository.findById(2L)).thenReturn(Optional.of(guest));

        assertThat(service.consume("valid-state", SocialProvider.GOOGLE)).isEqualTo(2L);
    }

    @Test
    void consumeRejectsStateAfterGuestWasAlreadyUpgraded() {
        User upgradedUser = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );
        upgradedUser.upgradeToUser("회원", LocalDateTime.now());
        when(redisRepository.consumeOAuthState("stale-state", "KAKAO")).thenReturn(Optional.of(2L));
        when(userRepository.findById(2L)).thenReturn(Optional.of(upgradedUser));

        assertThatThrownBy(() -> service.consume("stale-state", SocialProvider.KAKAO))
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
}
