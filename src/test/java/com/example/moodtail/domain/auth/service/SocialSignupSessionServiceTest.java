package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.model.SocialSignupSession;
import com.example.moodtail.domain.auth.model.SocialSignupTicket;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.auth.model.SocialUserProfile;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import com.example.moodtail.support.auth.AuthPropertiesFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialSignupSessionServiceTest {

    private static final String VALID_TOKEN = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    @Mock
    private RedisRepository redisRepository;

    private SocialSignupSessionService service;

    @BeforeEach
    void setUp() {
        service = new SocialSignupSessionService(
                redisRepository,
                AuthPropertiesFixtures.defaults()
        );
    }

    @Test
    void issueStoresProviderIdentityWithoutCreatingMemberSession() {
        SocialUserProfile profile = new SocialUserProfile(
                SocialProvider.GOOGLE,
                "google-user-id",
                "user@example.com",
                "제공자닉네임"
        );

        SocialSignupTicket ticket = service.issue(profile, 2L);

        assertThat(ticket.value()).hasSize(43);
        assertThat(ticket.expiresInSeconds()).isEqualTo(600L);
        ArgumentCaptor<RedisRepository.SocialSignupSession> sessionCaptor =
                ArgumentCaptor.forClass(RedisRepository.SocialSignupSession.class);
        verify(redisRepository).saveSocialSignupToken(
                eq(ticket.value()),
                sessionCaptor.capture(),
                eq(Duration.ofMinutes(10))
        );
        assertThat(sessionCaptor.getValue().provider()).isEqualTo("GOOGLE");
        assertThat(sessionCaptor.getValue().providerUserId()).isEqualTo("google-user-id");
        assertThat(sessionCaptor.getValue().email()).isEqualTo("user@example.com");
        assertThat(sessionCaptor.getValue().guestUserId()).isEqualTo(2L);
    }

    @Test
    void consumeReturnsAnonymousSignupSession() {
        when(redisRepository.consumeSocialSignupToken(VALID_TOKEN))
                .thenReturn(Optional.of(new RedisRepository.SocialSignupSession(
                        "KAKAO",
                        "kakao-user-id",
                        "user@example.com",
                        null
                )));

        SocialSignupSession result = service.consume(VALID_TOKEN);

        assertThat(result.provider()).isEqualTo(SocialProvider.KAKAO);
        assertThat(result.providerUserId()).isEqualTo("kakao-user-id");
        assertThat(result.guestUserId()).isNull();
    }

    @Test
    void consumeRejectsExpiredOrReusedToken() {
        when(redisRepository.consumeSocialSignupToken(VALID_TOKEN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consume(VALID_TOKEN))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH044")
                );
    }

    @Test
    void consumeRejectsMalformedStoredSession() {
        when(redisRepository.consumeSocialSignupToken(VALID_TOKEN))
                .thenReturn(Optional.of(new RedisRepository.SocialSignupSession(
                        "UNSUPPORTED",
                        "provider-user-id",
                        "user@example.com",
                        null
                )));

        assertThatThrownBy(() -> service.consume(VALID_TOKEN))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH044")
                );
    }

    @Test
    void consumeRejectsStoredSessionWithoutProvider() {
        when(redisRepository.consumeSocialSignupToken(VALID_TOKEN))
                .thenReturn(Optional.of(new RedisRepository.SocialSignupSession(
                        null,
                        "provider-user-id",
                        "user@example.com",
                        null
                )));

        assertThatThrownBy(() -> service.consume(VALID_TOKEN))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH044")
                );
    }
}
