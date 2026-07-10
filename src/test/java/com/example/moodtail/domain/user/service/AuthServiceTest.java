package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.client.OAuthClient;
import com.example.moodtail.domain.user.client.SocialUserProfile;
import com.example.moodtail.domain.user.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.user.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.user.dto.request.SocialSignupRequest;
import com.example.moodtail.domain.user.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.user.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.user.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.user.dto.response.SocialSignupResponse;
import com.example.moodtail.domain.user.dto.response.TokenResponse;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.enums.SocialProvider;
import com.example.moodtail.domain.user.enums.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.domain.user.support.AuthPropertiesFixtures;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.config.security.jwt.JwtProvider;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final long REFRESH_EXPIRATION_MILLIS = 1_209_600_000L;
    private static final UUID GUEST_UUID = UUID.fromString("b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d");

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisRepository redisRepository;

    @Mock
    private OAuthClient kakaoOAuthClient;

    @Mock
    private SocialAccountRegistrationService socialAccountRegistrationService;

    @Mock
    private GuestUserRegistrationService guestUserRegistrationService;

    @Mock
    private OAuthStateService oAuthStateService;

    @Mock
    private GuestLoginRateLimiter guestLoginRateLimiter;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                jwtProvider,
                redisRepository,
                List.of(kakaoOAuthClient),
                socialAccountRegistrationService,
                guestUserRegistrationService,
                oAuthStateService,
                guestLoginRateLimiter,
                AuthPropertiesFixtures.defaults()
        );
        ReflectionTestUtils.setField(authService, "jwtRefreshExpirationMillis", REFRESH_EXPIRATION_MILLIS);
    }

    @Test
    void guestLoginCreatesOrRestoresGuestAndIssuesGuestToken() {
        GuestLoginUser guest = new GuestLoginUser(2L, GUEST_UUID.toString(), UserRole.GUEST, true);
        TokenInfo tokenInfo = new TokenInfo("guest-access-token", "guest-refresh-token");
        Claims refreshClaims = refreshClaims("2", "guest-refresh-jti");

        when(guestUserRegistrationService.findOrCreate(GUEST_UUID)).thenReturn(guest);
        when(jwtProvider.generateToken(2L, UserRole.GUEST)).thenReturn(tokenInfo);
        when(jwtProvider.getRefreshTokenClaims("guest-refresh-token")).thenReturn(refreshClaims);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        GuestLoginResponse result = authService.guestLogin(new GuestLoginRequest(GUEST_UUID), request, response);

        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.guestUuid()).isEqualTo(GUEST_UUID.toString());
        assertThat(result.isNewUser()).isTrue();
        assertThat(result.accessToken()).isEqualTo("guest-access-token");
        assertRefreshCookie(response, "guest-refresh-token", 1_209_600);
        verify(guestLoginRateLimiter).check(GUEST_UUID, request);
        verify(redisRepository).saveRefreshJti(2L, "guest-refresh-jti");
    }

    @Test
    void createOAuthStateBindsProviderAndGuestUser() {
        OAuthStateService.OAuthState state = new OAuthStateService.OAuthState("state-value", 300L);
        when(oAuthStateService.issue(2L, SocialProvider.KAKAO)).thenReturn(state);

        OAuthStateResponse result = authService.createOAuthState("kakao", 2L);

        assertThat(result.state()).isEqualTo("state-value");
        assertThat(result.expiresInSeconds()).isEqualTo(300L);
    }

    @Test
    void socialLoginConsumesStateAndLogsInExistingAccountWithoutEmail() {
        SocialUserProfile profile = new SocialUserProfile(
                SocialProvider.KAKAO,
                "12345",
                null,
                "카카오유저"
        );
        SocialLoginUser socialUser = new SocialLoginUser(
                99L,
                UserRole.USER,
                "카카오유저",
                SocialProvider.KAKAO,
                null,
                false
        );
        TokenInfo tokenInfo = new TokenInfo("service-access-token", "service-refresh-token");
        when(oAuthStateService.consume("state-value", SocialProvider.KAKAO)).thenReturn(2L);
        when(kakaoOAuthClient.provider()).thenReturn(SocialProvider.KAKAO);
        when(kakaoOAuthClient.requestUserProfile("kakao-code", null)).thenReturn(profile);
        when(socialAccountRegistrationService.login(profile)).thenReturn(socialUser);
        when(jwtProvider.generateToken(99L, UserRole.USER)).thenReturn(tokenInfo);
        when(jwtProvider.getRefreshTokenClaims("service-refresh-token"))
                .thenReturn(refreshClaims("99", "service-refresh-jti"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        SocialLoginResponse result = authService.socialLogin(
                "kakao",
                new SocialLoginRequest("kakao-code", null, "state-value"),
                response
        );

        assertThat(result.userId()).isEqualTo(99L);
        assertThat(result.email()).isNull();
        assertThat(result.provider()).isEqualTo(SocialProvider.KAKAO);
        assertThat(result.isNewUser()).isFalse();
        verify(redisRepository).deleteRefreshJti(2L);
        verify(redisRepository).saveRefreshJti(99L, "service-refresh-jti");
    }

    @Test
    void socialSignupUpgradesGuestAndIssuesUserToken() {
        SocialUserProfile providerProfile = new SocialUserProfile(
                SocialProvider.KAKAO,
                "12345",
                null,
                "카카오닉네임"
        );
        SocialUserProfile signupProfile = new SocialUserProfile(
                SocialProvider.KAKAO,
                "12345",
                null,
                "요청닉네임"
        );
        SocialLoginUser signupUser = new SocialLoginUser(
                2L,
                UserRole.USER,
                "요청닉네임",
                SocialProvider.KAKAO,
                null,
                true
        );
        TokenInfo tokenInfo = new TokenInfo("signup-access-token", "signup-refresh-token");
        when(oAuthStateService.consume("state-value", SocialProvider.KAKAO)).thenReturn(2L);
        when(kakaoOAuthClient.provider()).thenReturn(SocialProvider.KAKAO);
        when(kakaoOAuthClient.requestUserProfile("kakao-code", null)).thenReturn(providerProfile);
        when(socialAccountRegistrationService.register(eq(signupProfile), eq(2L), anyList()))
                .thenReturn(signupUser);
        when(jwtProvider.generateToken(2L, UserRole.USER)).thenReturn(tokenInfo);
        when(jwtProvider.getRefreshTokenClaims("signup-refresh-token"))
                .thenReturn(refreshClaims("2", "signup-refresh-jti"));

        SocialSignupResponse result = authService.socialSignup(
                new SocialSignupRequest(
                        "kakao",
                        "kakao-code",
                        null,
                        "state-value",
                        "요청닉네임",
                        List.of(new SocialSignupRequest.Agreement(1L, true))
                ),
                new MockHttpServletResponse()
        );

        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.nickname()).isEqualTo("요청닉네임");
        assertThat(result.provider()).isEqualTo(SocialProvider.KAKAO);
        assertThat(result.isNewUser()).isTrue();
        verify(socialAccountRegistrationService).register(eq(signupProfile), eq(2L), anyList());
        verify(redisRepository).saveRefreshJti(2L, "signup-refresh-jti");
    }



    @Test
    void socialLoginRejectsProfileWithoutProviderUserId() {
        SocialUserProfile invalidProfile = new SocialUserProfile(
                SocialProvider.KAKAO,
                null,
                null,
                "카카오유저"
        );
        when(oAuthStateService.consume("state-value", SocialProvider.KAKAO)).thenReturn(2L);
        when(kakaoOAuthClient.provider()).thenReturn(SocialProvider.KAKAO);
        when(kakaoOAuthClient.requestUserProfile("kakao-code", null)).thenReturn(invalidProfile);

        assertThatThrownBy(() -> authService.socialLogin(
                "kakao",
                new SocialLoginRequest("kakao-code", null, "state-value"),
                new MockHttpServletResponse()
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH016")
        );

        verify(socialAccountRegistrationService, never()).login(any());
        verify(socialAccountRegistrationService, never()).register(any(), any(), anyList());
    }

    @Test
    void socialLoginRejectsUnsupportedProviderBeforeConsumingState() {
        assertThatThrownBy(() -> authService.socialLogin(
                "unsupported",
                new SocialLoginRequest("code", null, "state-value"),
                new MockHttpServletResponse()
        )).isInstanceOf(RestApiException.class);

        verify(oAuthStateService, never()).consume(any(), any());
    }

    @Test
    void reissueRotatesRefreshTokenWhenStoredJtiMatches() {
        User user = socialUserWithId(1L);
        Claims oldRefreshClaims = refreshClaims("1", "old-refresh-jti");
        Claims newRefreshClaims = refreshClaims("1", "new-refresh-jti");
        TokenInfo newTokenInfo = new TokenInfo("new-access-token", "new-refresh-token");

        when(jwtProvider.getRefreshTokenClaims("old-refresh-token")).thenReturn(oldRefreshClaims);
        when(jwtProvider.validateRefreshToken("old-refresh-token")).thenReturn(true);
        when(redisRepository.findRefreshJtiByUserId(1L)).thenReturn(Optional.of("old-refresh-jti"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtProvider.generateToken(1L, UserRole.USER)).thenReturn(newTokenInfo);
        when(jwtProvider.getRefreshTokenClaims("new-refresh-token")).thenReturn(newRefreshClaims);
        when(redisRepository.replaceRefreshJti(1L, "old-refresh-jti", "new-refresh-jti")).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "old-refresh-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        TokenResponse result = authService.reissue(request, response);

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertRefreshCookie(response, "new-refresh-token", 1_209_600);
    }

    @Test
    void reissueRejectsRefreshTokenWhenStoredJtiDoesNotMatch() {
        Claims oldRefreshClaims = refreshClaims("1", "old-refresh-jti");
        when(jwtProvider.getRefreshTokenClaims("old-refresh-token")).thenReturn(oldRefreshClaims);
        when(jwtProvider.validateRefreshToken("old-refresh-token")).thenReturn(true);
        when(redisRepository.findRefreshJtiByUserId(1L)).thenReturn(Optional.of("different-jti"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "old-refresh-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> authService.reissue(request, response))
                .isInstanceOf(RestApiException.class);
        assertClearedRefreshCookie(response);
    }

    @Test
    void logoutBlocksAccessTokenAndDeletesRefreshJti() {
        Claims accessClaims = accessClaims("1", "access-jti");
        when(jwtProvider.resolveToken(any())).thenReturn("access-token");
        when(jwtProvider.validateAccessToken("access-token")).thenReturn(true);
        when(jwtProvider.getAccessTokenClaims("access-token")).thenReturn(accessClaims);

        MockHttpServletResponse response = new MockHttpServletResponse();
        authService.logout(new MockHttpServletRequest(), response);

        assertClearedRefreshCookie(response);
        verify(redisRepository).blockAccessToken("access-token", accessClaims);
        verify(redisRepository).deleteRefreshJti(1L);
    }

    private User socialUserWithId(Long id) {
        User user = User.createGuest(GUEST_UUID.toString(), "게스트", LocalDateTime.now());
        user.upgradeToUser("테스터", LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Claims refreshClaims(String subject, String jti) {
        Claims claims = Jwts.claims().setSubject(subject).setId(jti);
        claims.put("role", "USER");
        claims.put("tokenType", "REFRESH");
        return claims;
    }

    private Claims accessClaims(String subject, String jti) {
        Claims claims = Jwts.claims().setSubject(subject).setId(jti);
        claims.put("role", "USER");
        claims.put("tokenType", "ACCESS");
        return claims;
    }

    private void assertRefreshCookie(MockHttpServletResponse response, String value, int maxAge) {
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).contains(
                "refreshToken=" + value,
                "Path=/",
                "Max-Age=" + maxAge,
                "HttpOnly",
                "SameSite=Lax"
        );
    }

    private void assertClearedRefreshCookie(MockHttpServletResponse response) {
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains("refreshToken=", "Path=/", "Max-Age=0", "HttpOnly", "SameSite=Lax");
    }
}
