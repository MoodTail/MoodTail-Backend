package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.dto.response.AuthResponse;
import com.example.moodtail.domain.user.entity.SocialAccount;
import com.example.moodtail.domain.user.entity.SocialProvider;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.model.SocialUserProfile;
import com.example.moodtail.domain.user.repository.SocialAccountRepository;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.config.security.jwt.JwtProvider;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final TokenInfo TOKEN_INFO = new TokenInfo("access-token", "refresh-token");
    private static final Claims REFRESH_CLAIMS = Jwts.claims().setId("refresh-jti");

    @Mock
    private UserRepository userRepository;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisRepository redisRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginOrSignupSocialCreatesUserAndSocialAccountWhenFirstLogin() {
        SocialUserProfile profile = new SocialUserProfile("kakao-123", "user@example.com", "테스터");
        User savedUser = userWithId(User.createSocialUser("테스터"), 1L);

        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "kakao-123"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtProvider.generateToken(1L, "ROLE_USER")).thenReturn(TOKEN_INFO);
        when(jwtProvider.getClaims("refresh-token")).thenReturn(REFRESH_CLAIMS);

        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthResponse result = authService.loginOrSignupSocial(SocialProvider.KAKAO, profile, response);

        assertThat(result.isNewUser()).isTrue();
        assertThat(result.user().userId()).isEqualTo(1L);
        assertThat(result.user().nickname()).isEqualTo("테스터");
        assertThat(result.token().accessToken()).isEqualTo("access-token");
        assertThat(result.token().refreshToken()).isEqualTo("refresh-token");
        assertThat(response.getCookie("refreshToken")).isNotNull();

        ArgumentCaptor<SocialAccount> socialAccountCaptor = ArgumentCaptor.forClass(SocialAccount.class);
        verify(socialAccountRepository).save(socialAccountCaptor.capture());
        SocialAccount socialAccount = socialAccountCaptor.getValue();
        assertThat(socialAccount.getUser()).isEqualTo(savedUser);
        assertThat(socialAccount.getEmail()).isEqualTo("user@example.com");
        assertThat(socialAccount.getProvider()).isEqualTo(SocialProvider.KAKAO);
        assertThat(socialAccount.getProviderUserId()).isEqualTo("kakao-123");
        verify(redisRepository).saveRefreshJti(1L, "refresh-jti");
    }

    @Test
    void loginOrSignupSocialUsesExistingUserWhenSocialAccountExists() {
        User user = userWithId(User.createSocialUser("기존유저"), 2L);
        SocialAccount socialAccount = SocialAccount.create(
                user,
                "google@example.com",
                SocialProvider.GOOGLE,
                "google-123"
        );

        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.GOOGLE, "google-123"))
                .thenReturn(Optional.of(socialAccount));
        when(jwtProvider.generateToken(2L, "ROLE_USER")).thenReturn(TOKEN_INFO);
        when(jwtProvider.getClaims("refresh-token")).thenReturn(REFRESH_CLAIMS);

        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthResponse result = authService.loginOrSignupSocial(
                SocialProvider.GOOGLE,
                new SocialUserProfile("google-123", "google@example.com", "ignored"),
                response
        );

        assertThat(result.isNewUser()).isFalse();
        assertThat(result.user().userId()).isEqualTo(2L);
        assertThat(result.user().nickname()).isEqualTo("기존유저");
        verify(userRepository, never()).save(any(User.class));
        verify(socialAccountRepository, never()).save(any(SocialAccount.class));
        verify(redisRepository).saveRefreshJti(2L, "refresh-jti");
    }

    @Test
    void loginOrSignupSocialRejectsProfileWithoutRequiredEmail() {
        assertThatThrownBy(() -> authService.loginOrSignupSocial(
                SocialProvider.KAKAO,
                new SocialUserProfile("kakao-123", "", "테스터"),
                new MockHttpServletResponse()
        )).isInstanceOf(RestApiException.class);
    }

    private User userWithId(User user, Long id) {
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
