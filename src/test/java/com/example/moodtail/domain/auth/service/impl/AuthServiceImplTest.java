package com.example.moodtail.domain.auth.service.impl;

import com.example.moodtail.domain.auth.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.auth.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.auth.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.auth.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.auth.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.auth.dto.response.TokenResponse;
import com.example.moodtail.domain.auth.model.ConsumedOAuthState;
import com.example.moodtail.domain.auth.model.GuestLoginUser;
import com.example.moodtail.domain.auth.model.SocialAuthenticationResult;
import com.example.moodtail.domain.auth.model.OAuthState;
import com.example.moodtail.domain.auth.model.SocialLoginUser;
import com.example.moodtail.domain.auth.service.GuestUserRegistrationService;
import com.example.moodtail.domain.auth.service.LocalAccountService;
import com.example.moodtail.domain.auth.service.OAuthStateService;
import com.example.moodtail.domain.auth.service.PasswordResetService;
import com.example.moodtail.domain.auth.service.SocialAccountService;
import com.example.moodtail.domain.auth.service.TokenSessionService;
import com.example.moodtail.domain.auth.validator.AuthRequestOriginValidator;
import com.example.moodtail.domain.auth.validator.GuestLoginRateLimiter;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.auth.client.OAuthClient;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.auth.model.SocialUserProfile;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.config.security.jwt.JwtProvider;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import com.example.moodtail.support.auth.AuthPropertiesFixtures;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final long REFRESH_EXPIRATION_MILLIS = 1_209_600_000L;
    private static final UUID GUEST_UUID = UUID.fromString("b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d");
    private static final String PKCE_VERIFIER =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_";

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisRepository redisRepository;

    @Mock
    private OAuthClient kakaoOAuthClient;

    @Mock
    private OAuthClient googleOAuthClient;

    @Mock
    private SocialAccountService socialAccountService;

    @Mock
    private GuestUserRegistrationService guestUserRegistrationService;

    @Mock
    private OAuthStateService oAuthStateService;

    @Mock
    private GuestLoginRateLimiter guestLoginRateLimiter;

    @Mock
    private LocalAccountService localAccountService;

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private AuthRequestOriginValidator authRequestOriginValidator;

    private TokenSessionService tokenSessionService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        lenient().when(kakaoOAuthClient.provider()).thenReturn(SocialProvider.KAKAO);
        lenient().when(kakaoOAuthClient.isEnabled()).thenReturn(true);
        lenient().when(googleOAuthClient.provider()).thenReturn(SocialProvider.GOOGLE);
        lenient().when(googleOAuthClient.isEnabled()).thenReturn(true);
        lenient().when(redisRepository.findRefreshJtiByUserId(any())).thenReturn(Optional.empty());
        tokenSessionService = new TokenSessionService(
                userRepository,
                jwtProvider,
                redisRepository,
                authRequestOriginValidator,
                AuthPropertiesFixtures.defaults()
        );
        ReflectionTestUtils.setField(
                tokenSessionService,
                "jwtRefreshExpirationMillis",
                REFRESH_EXPIRATION_MILLIS
        );
        authService = new AuthServiceImpl(
                List.of(kakaoOAuthClient, googleOAuthClient),
                socialAccountService,
                guestUserRegistrationService,
                oAuthStateService,
                guestLoginRateLimiter,
                localAccountService,
                passwordResetService,
                tokenSessionService
        );
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
        OAuthState state = new OAuthState("state-value", 300L);
        when(oAuthStateService.issue(2L, SocialProvider.KAKAO)).thenReturn(state);

        OAuthStateResponse result = authService.createOAuthState("kakao", 2L);

        assertThat(result.state()).isEqualTo("state-value");
        assertThat(result.expiresInSeconds()).isEqualTo(300L);
    }

    @Test
    void disabledGoogleProviderDoesNotIssueOrConsumeOAuthState() {
        when(googleOAuthClient.isEnabled()).thenReturn(false);

        assertThatThrownBy(() -> authService.createOAuthState("google", 2L))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH017")
                );
        assertThatThrownBy(() -> authService.socialLogin(
                "google",
                new SocialLoginRequest("google-code", null, "google-state"),
                new MockHttpServletResponse()
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH017")
        );

        verify(oAuthStateService, never()).issue(any(), any());
        verify(oAuthStateService, never()).consumeForAuthentication(any(), any());
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
        when(oAuthStateService.consumeForAuthentication("state-value", SocialProvider.KAKAO))
                .thenReturn(new ConsumedOAuthState(2L, PKCE_VERIFIER));
        when(kakaoOAuthClient.provider()).thenReturn(SocialProvider.KAKAO);
        when(kakaoOAuthClient.requestUserProfile("kakao-code", null, PKCE_VERIFIER)).thenReturn(profile);
        when(socialAccountService.authenticate(eq(profile), eq(2L), any()))
                .thenReturn(new SocialAuthenticationResult(socialUser, tokenInfo));

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
        verify(socialAccountService).authenticate(eq(profile), eq(2L), any());
    }

    @Test
    void socialLoginRegistersUnregisteredAccountInSameRequest() {
        SocialUserProfile profile = new SocialUserProfile(
                SocialProvider.GOOGLE,
                "unregistered-google-id",
                "new-user@example.com",
                "신규사용자"
        );
        when(oAuthStateService.consumeForAuthentication("state-value", SocialProvider.GOOGLE))
                .thenReturn(new ConsumedOAuthState(2L, PKCE_VERIFIER));
        when(googleOAuthClient.requestUserProfile("google-code", null, PKCE_VERIFIER)).thenReturn(profile);
        SocialLoginUser newUser = new SocialLoginUser(
                2L, UserRole.USER, "신규사용자", SocialProvider.GOOGLE, "new-user@example.com", true
        );
        TokenInfo tokenInfo = new TokenInfo("new-access", "new-refresh");
        when(socialAccountService.authenticate(eq(profile), eq(2L), anyList()))
                .thenReturn(new SocialAuthenticationResult(newUser, tokenInfo));

        SocialLoginResponse result = authService.socialLogin(
                "google",
                new SocialLoginRequest(
                        "google-code",
                        null,
                        "state-value",
                        "신규사용자",
                        List.of(new com.example.moodtail.domain.auth.dto.request.TermAgreementRequest(1L, true))
                ),
                new MockHttpServletResponse()
        );

        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.isNewUser()).isTrue();
        assertThat(result.accessToken()).isEqualTo("new-access");
    }

    @Test
    void socialLoginDoesNotMergeGuestWhenProviderAccountAlreadyExists() {
        SocialUserProfile profile = new SocialUserProfile(
                SocialProvider.GOOGLE,
                "google-user-id",
                "user@example.com",
                "기존유저"
        );
        SocialLoginUser existingUser = new SocialLoginUser(
                99L,
                UserRole.USER,
                "기존유저",
                SocialProvider.GOOGLE,
                "user@example.com",
                false
        );
        TokenInfo tokenInfo = new TokenInfo("access", "refresh");

        when(oAuthStateService.consumeForAuthentication("state-value", SocialProvider.GOOGLE))
                .thenReturn(new ConsumedOAuthState(2L, PKCE_VERIFIER));
        when(googleOAuthClient.provider()).thenReturn(SocialProvider.GOOGLE);
        when(googleOAuthClient.requestUserProfile(
                "google-code", "http://frontend/callback", PKCE_VERIFIER
        ))
                .thenReturn(profile);
        when(socialAccountService.authenticate(eq(profile), eq(2L), any()))
                .thenReturn(new SocialAuthenticationResult(existingUser, tokenInfo));

        SocialLoginResponse result = authService.socialLogin(
                "google",
                new SocialLoginRequest("google-code", "http://frontend/callback", "state-value"),
                new MockHttpServletResponse()
        );

        assertThat(result.userId()).isEqualTo(99L);
        assertThat(result.isNewUser()).isFalse();
        verify(socialAccountService).authenticate(eq(profile), eq(2L), any());
    }

    @Test
    void socialLoginRejectsProfileWithoutProviderUserId() {
        SocialUserProfile invalidProfile = new SocialUserProfile(
                SocialProvider.KAKAO,
                null,
                null,
                "카카오유저"
        );
        when(oAuthStateService.consumeForAuthentication("state-value", SocialProvider.KAKAO))
                .thenReturn(new ConsumedOAuthState(2L, PKCE_VERIFIER));
        when(kakaoOAuthClient.provider()).thenReturn(SocialProvider.KAKAO);
        when(kakaoOAuthClient.requestUserProfile("kakao-code", null, PKCE_VERIFIER)).thenReturn(invalidProfile);

        assertThatThrownBy(() -> authService.socialLogin(
                "kakao",
                new SocialLoginRequest("kakao-code", null, "state-value"),
                new MockHttpServletResponse()
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH016")
        );

        verify(socialAccountService, never()).authenticate(any(), any(), any());
    }

    @Test
    void socialLoginRejectsUnsupportedProviderBeforeConsumingState() {
        assertThatThrownBy(() -> authService.socialLogin(
                "unsupported",
                new SocialLoginRequest("code", null, "state-value"),
                new MockHttpServletResponse()
        )).isInstanceOf(RestApiException.class);

        verify(oAuthStateService, never()).consumeForAuthentication(any(), any());
    }

    @Test
    void oauthClientRegistryRejectsDuplicateProviderAdapters() {
        AuthServiceImpl invalidService = new AuthServiceImpl(
                List.of(kakaoOAuthClient, kakaoOAuthClient, googleOAuthClient),
                socialAccountService,
                guestUserRegistrationService,
                oAuthStateService,
                guestLoginRateLimiter,
                localAccountService,
                passwordResetService,
                tokenSessionService
        );

        assertThatThrownBy(invalidService::validateOAuthClients)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly one");
    }

    @Test
    void oauthClientRegistryRejectsMissingProviderAdapter() {
        AuthServiceImpl invalidService = new AuthServiceImpl(
                List.of(kakaoOAuthClient),
                socialAccountService,
                guestUserRegistrationService,
                oAuthStateService,
                guestLoginRateLimiter,
                localAccountService,
                passwordResetService,
                tokenSessionService
        );

        assertThatThrownBy(invalidService::validateOAuthClients)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Every social provider");
    }

    @Test
    void reissueRotatesRefreshTokenWhenStoredJtiMatches() {
        User user = socialUserWithId(1L);
        Claims oldRefreshClaims = refreshClaims("1", "old-refresh-jti");
        Claims newRefreshClaims = refreshClaims("1", "new-refresh-jti");
        TokenInfo newTokenInfo = new TokenInfo("new-access-token", "new-refresh-token");

        when(jwtProvider.getRefreshTokenClaims("old-refresh-token")).thenReturn(oldRefreshClaims);
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
        when(redisRepository.findRefreshJtiByUserId(1L)).thenReturn(Optional.of("different-jti"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "old-refresh-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> authService.reissue(request, response))
                .isInstanceOf(RestApiException.class);
        assertClearedRefreshCookie(response);
    }

    @Test
    void reissueRejectsSoftDeletedUserAndRemovesServerSession() {
        User deletedUser = socialUserWithId(1L);
        deletedUser.delete();
        Claims oldRefreshClaims = refreshClaims("1", "old-refresh-jti");
        when(jwtProvider.getRefreshTokenClaims("old-refresh-token")).thenReturn(oldRefreshClaims);
        when(redisRepository.findRefreshJtiByUserId(1L)).thenReturn(Optional.of("old-refresh-jti"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(deletedUser));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "old-refresh-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> authService.reissue(request, response))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH020")
                );

        verify(redisRepository).deleteRefreshJti(1L);
        verify(jwtProvider, never()).generateToken(any(), any());
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
        verify(redisRepository).blockAccessToken(accessClaims);
        verify(redisRepository).deleteRefreshJti(1L);
    }

    @Test
    void logoutUsesMatchingRefreshCookieWhenAccessTokenIsUnavailable() {
        Claims refreshClaims = refreshClaims("1", "current-refresh-jti");
        when(jwtProvider.getRefreshTokenClaims("refresh-token")).thenReturn(refreshClaims);
        when(redisRepository.findRefreshJtiByUserId(1L)).thenReturn(Optional.of("current-refresh-jti"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "refresh-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        authService.logout(request, response);

        verify(redisRepository).deleteRefreshJti(1L);
        assertClearedRefreshCookie(response);
    }

    @Test
    void logoutRevokesAccessAndRefreshSessionsEvenWhenTheyBelongToDifferentUsers() {
        Claims accessClaims = accessClaims("1", "access-jti");
        Claims refreshClaims = refreshClaims("2", "refresh-jti");
        when(jwtProvider.resolveToken(any())).thenReturn("access-token");
        when(jwtProvider.validateAccessToken("access-token")).thenReturn(true);
        when(jwtProvider.getAccessTokenClaims("access-token")).thenReturn(accessClaims);
        when(jwtProvider.getRefreshTokenClaims("refresh-token")).thenReturn(refreshClaims);
        when(redisRepository.findRefreshJtiByUserId(2L)).thenReturn(Optional.of("refresh-jti"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "refresh-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        authService.logout(request, response);

        verify(redisRepository).blockAccessToken(accessClaims);
        verify(redisRepository).deleteRefreshJti(1L);
        verify(redisRepository).deleteRefreshJti(2L);
        assertClearedRefreshCookie(response);
    }

    @Test
    void logoutDoesNotDeleteCurrentSessionForStaleRefreshCookie() {
        Claims refreshClaims = refreshClaims("1", "stale-refresh-jti");
        when(jwtProvider.getRefreshTokenClaims("stale-refresh-token")).thenReturn(refreshClaims);
        when(redisRepository.findRefreshJtiByUserId(1L)).thenReturn(Optional.of("current-refresh-jti"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "stale-refresh-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        authService.logout(request, response);

        verify(redisRepository, never()).deleteRefreshJti(any());
        assertClearedRefreshCookie(response);
    }

    @Test
    void logoutWithoutTokensIsIdempotent() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        authService.logout(new MockHttpServletRequest(), response);

        verify(redisRepository, never()).deleteRefreshJti(any());
        assertClearedRefreshCookie(response);
    }

    @Test
    void logoutKeepsCookieForRetryWhenRevocationCannotBeStored() {
        Claims accessClaims = accessClaims("1", "access-jti");
        when(jwtProvider.resolveToken(any())).thenReturn("access-token");
        when(jwtProvider.validateAccessToken("access-token")).thenReturn(true);
        when(jwtProvider.getAccessTokenClaims("access-token")).thenReturn(accessClaims);
        org.mockito.Mockito.doThrow(new RedisConnectionFailureException("redis unavailable"))
                .when(redisRepository)
                .blockAccessToken(accessClaims);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> authService.logout(new MockHttpServletRequest(), response))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH028")
                );

        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }

    @Test
    void reissueKeepsRefreshCookieWhenRedisIsTemporarilyUnavailable() {
        Claims oldRefreshClaims = refreshClaims("1", "old-refresh-jti");
        when(jwtProvider.getRefreshTokenClaims("old-refresh-token")).thenReturn(oldRefreshClaims);
        when(redisRepository.findRefreshJtiByUserId(1L))
                .thenThrow(new RedisConnectionFailureException("redis unavailable"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "old-refresh-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> authService.reissue(request, response))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH028")
                );

        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
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
