package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.auth.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.auth.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.auth.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.auth.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.auth.dto.response.TokenResponse;
import com.example.moodtail.domain.auth.model.AuthResult;
import com.example.moodtail.domain.auth.model.ConsumedOAuthState;
import com.example.moodtail.domain.auth.model.GuestLoginUser;
import com.example.moodtail.domain.auth.model.SocialAuthenticationResult;
import com.example.moodtail.domain.auth.model.OAuthState;
import com.example.moodtail.domain.auth.model.SocialLoginUser;
import com.example.moodtail.domain.auth.validator.GuestLoginRateLimiter;
import com.example.moodtail.domain.auth.validator.LocalAuthRateLimiter;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.global.auth.client.OAuthClient;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.auth.model.SocialUserProfile;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.config.security.jwt.JwtProvider;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final UUID GUEST_UUID = UUID.fromString("b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d");
    private static final String PKCE_VERIFIER =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

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
    private GuestUserService guestUserService;

    @Mock
    private OAuthStateService oAuthStateService;

    @Mock
    private GuestLoginRateLimiter guestLoginRateLimiter;

    @Mock
    private LocalAuthRateLimiter localAuthRateLimiter;

    @Mock
    private LocalAccountService localAccountService;

    @Mock
    private PasswordResetService passwordResetService;

    private TokenSessionService tokenSessionService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        lenient().when(kakaoOAuthClient.provider()).thenReturn(SocialProvider.KAKAO);
        lenient().when(kakaoOAuthClient.isEnabled()).thenReturn(true);
        lenient().when(googleOAuthClient.provider()).thenReturn(SocialProvider.GOOGLE);
        lenient().when(googleOAuthClient.isEnabled()).thenReturn(true);
        lenient().when(redisRepository.findRefreshJtiByUserId(any())).thenReturn(Optional.empty());
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        tokenSessionService = new TokenSessionService(
                userRepository,
                transactionManager,
                jwtProvider,
                redisRepository
        );
        authService = new AuthService(
                List.of(kakaoOAuthClient, googleOAuthClient),
                socialAccountService,
                guestUserService,
                oAuthStateService,
                guestLoginRateLimiter,
                localAuthRateLimiter,
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

        when(guestUserService.findOrCreate(GUEST_UUID)).thenReturn(guest);
        when(jwtProvider.generateToken(2L, UserRole.GUEST)).thenReturn(tokenInfo);
        when(jwtProvider.getRefreshTokenClaims("guest-refresh-token")).thenReturn(refreshClaims);

        AuthResult<GuestLoginResponse> result = authService.guestLogin(
                new GuestLoginRequest(GUEST_UUID),
                "203.0.113.7"
        );

        assertThat(result.response().userId()).isEqualTo(2L);
        assertThat(result.response().guestUuid()).isEqualTo(GUEST_UUID.toString());
        assertThat(result.response().isNewUser()).isTrue();
        assertThat(result.response().accessToken()).isEqualTo("guest-access-token");
        assertThat(result.refreshToken()).isEqualTo("guest-refresh-token");
        verify(guestLoginRateLimiter).check(GUEST_UUID, "203.0.113.7");
        verify(redisRepository).saveRefreshJti(2L, "guest-refresh-jti");
    }

    @Test
    void createOAuthStateBindsProviderAndGuestUser() {
        OAuthState state = new OAuthState("state-value", "challenge", "S256", 300L);
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
                new SocialLoginRequest("google-code", null, "google-state")
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH017")
        );

        verify(oAuthStateService, never()).issue(any(), any());
        verify(oAuthStateService, never()).consumeForAuthentication(any(), any());
    }

    @Test
    void socialLoginConsumesStateAndLogsInExistingAccount() {
        SocialUserProfile profile = new SocialUserProfile(
                SocialProvider.KAKAO,
                "12345",
                "kakao@example.com",
                "카카오유저"
        );
        SocialLoginUser socialUser = new SocialLoginUser(
                99L,
                UserRole.USER,
                "카카오유저",
                SocialProvider.KAKAO,
                "kakao@example.com",
                false
        );
        TokenInfo tokenInfo = new TokenInfo("service-access-token", "service-refresh-token");
        when(oAuthStateService.consumeForAuthentication("state-value", SocialProvider.KAKAO))
                .thenReturn(new ConsumedOAuthState(2L, PKCE_VERIFIER));
        when(kakaoOAuthClient.provider()).thenReturn(SocialProvider.KAKAO);
        when(kakaoOAuthClient.requestUserProfile("kakao-code", null, PKCE_VERIFIER)).thenReturn(profile);
        when(socialAccountService.authenticate(eq(profile), eq(2L), any()))
                .thenReturn(new SocialAuthenticationResult(socialUser, tokenInfo));

        AuthResult<SocialLoginResponse> result = authService.socialLogin(
                "kakao",
                new SocialLoginRequest("kakao-code", null, "state-value")
        );

        assertThat(result.response().userId()).isEqualTo(99L);
        assertThat(result.response().email()).isEqualTo("kakao@example.com");
        assertThat(result.response().provider()).isEqualTo(SocialProvider.KAKAO);
        assertThat(result.response().isNewUser()).isFalse();
        assertThat(result.refreshToken()).isEqualTo("service-refresh-token");
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

        AuthResult<SocialLoginResponse> result = authService.socialLogin(
                "google",
                new SocialLoginRequest(
                        "google-code",
                        null,
                        "state-value",
                        "신규사용자",
                        List.of(new com.example.moodtail.domain.auth.dto.request.TermAgreementRequest(1L, true))
                )
        );

        assertThat(result.response().userId()).isEqualTo(2L);
        assertThat(result.response().isNewUser()).isTrue();
        assertThat(result.response().accessToken()).isEqualTo("new-access");
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

        AuthResult<SocialLoginResponse> result = authService.socialLogin(
                "google",
                new SocialLoginRequest("google-code", "http://frontend/callback", "state-value")
        );

        assertThat(result.response().userId()).isEqualTo(99L);
        assertThat(result.response().isNewUser()).isFalse();
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
                new SocialLoginRequest("kakao-code", null, "state-value")
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH016")
        );

        verify(socialAccountService, never()).authenticate(any(), any(), any());
    }

    @Test
    void socialLoginRejectsProfileWithoutRequiredEmail() {
        SocialUserProfile invalidProfile = new SocialUserProfile(
                SocialProvider.KAKAO,
                "12345",
                null,
                "카카오유저"
        );
        when(oAuthStateService.consumeForAuthentication("state-value", SocialProvider.KAKAO))
                .thenReturn(new ConsumedOAuthState(2L, PKCE_VERIFIER));
        when(kakaoOAuthClient.requestUserProfile("kakao-code", null, PKCE_VERIFIER))
                .thenReturn(invalidProfile);

        assertThatThrownBy(() -> authService.socialLogin(
                "kakao",
                new SocialLoginRequest("kakao-code", null, "state-value")
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH016")
        );

        verify(socialAccountService, never()).authenticate(any(), any(), any());
    }

    @Test
    void socialLoginRejectsUnsupportedProviderBeforeConsumingState() {
        assertThatThrownBy(() -> authService.socialLogin(
                "unsupported",
                new SocialLoginRequest("code", null, "state-value")
        )).isInstanceOf(RestApiException.class);

        verify(oAuthStateService, never()).consumeForAuthentication(any(), any());
    }

    @Test
    void invalidOAuthRequestDoesNotConsumeTheOneTimeState() {
        org.mockito.Mockito.doThrow(new RestApiException(
                        com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.INVALID_SOCIAL_LOGIN
                ))
                .when(kakaoOAuthClient)
                .validateAuthorizationRequest("invalid-code", "https://app.example/callback");

        assertThatThrownBy(() -> authService.socialLogin(
                "kakao",
                new SocialLoginRequest(
                        "invalid-code",
                        "https://app.example/callback",
                        "state-value"
                )
        )).isInstanceOf(RestApiException.class);

        verify(oAuthStateService, never()).consumeForAuthentication(any(), any());
        verify(kakaoOAuthClient, never()).requestUserProfile(any(), any(), any());
    }

    @Test
    void existingSocialLoginLeavesSignupOnlyNicknameValidationToTheAccountService() {
        SocialUserProfile providerProfile = new SocialUserProfile(
                SocialProvider.KAKAO,
                "12345",
                "user@example.com",
                "제공자닉네임"
        );
        SocialUserProfile requestedProfile = new SocialUserProfile(
                SocialProvider.KAKAO,
                "12345",
                "user@example.com",
                "x"
        );
        SocialLoginUser existingUser = new SocialLoginUser(
                99L,
                UserRole.USER,
                "기존회원",
                SocialProvider.KAKAO,
                "user@example.com",
                false
        );
        TokenInfo tokenInfo = new TokenInfo("access", "refresh");
        when(oAuthStateService.consumeForAuthentication("state-value", SocialProvider.KAKAO))
                .thenReturn(new ConsumedOAuthState(2L, PKCE_VERIFIER));
        when(kakaoOAuthClient.requestUserProfile("kakao-code", null, PKCE_VERIFIER))
                .thenReturn(providerProfile);
        when(socialAccountService.authenticate(eq(requestedProfile), eq(2L), anyList()))
                .thenReturn(new SocialAuthenticationResult(existingUser, tokenInfo));

        AuthResult<SocialLoginResponse> result = authService.socialLogin(
                "kakao",
                new SocialLoginRequest(
                        "kakao-code",
                        null,
                        "state-value",
                        "x",
                        List.of()
                )
        );

        assertThat(result.response().userId()).isEqualTo(99L);
        assertThat(result.response().isNewUser()).isFalse();
        verify(socialAccountService).authenticate(eq(requestedProfile), eq(2L), anyList());
    }

    @Test
    void oauthClientRegistryRejectsDuplicateProviderAdapters() {
        AuthService invalidService = new AuthService(
                List.of(kakaoOAuthClient, kakaoOAuthClient, googleOAuthClient),
                socialAccountService,
                guestUserService,
                oAuthStateService,
                guestLoginRateLimiter,
                localAuthRateLimiter,
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
        AuthService invalidService = new AuthService(
                List.of(kakaoOAuthClient),
                socialAccountService,
                guestUserService,
                oAuthStateService,
                guestLoginRateLimiter,
                localAuthRateLimiter,
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
        when(userRepository.findAuthUserById(1L)).thenReturn(Optional.of(user));
        when(jwtProvider.generateToken(1L, UserRole.USER)).thenReturn(newTokenInfo);
        when(jwtProvider.getRefreshTokenClaims("new-refresh-token")).thenReturn(newRefreshClaims);
        when(redisRepository.replaceRefreshJti(1L, "old-refresh-jti", "new-refresh-jti")).thenReturn(true);

        AuthResult<TokenResponse> result = authService.reissue("old-refresh-token");

        assertThat(result.response().accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        InOrder order = inOrder(redisRepository, transactionManager);
        order.verify(redisRepository).findRefreshJtiByUserId(1L);
        order.verify(transactionManager).commit(any());
        order.verify(redisRepository).replaceRefreshJti(1L, "old-refresh-jti", "new-refresh-jti");
    }

    @Test
    void reissueRejectsRefreshTokenWhenStoredJtiDoesNotMatch() {
        Claims oldRefreshClaims = refreshClaims("1", "old-refresh-jti");
        when(jwtProvider.getRefreshTokenClaims("old-refresh-token")).thenReturn(oldRefreshClaims);
        when(redisRepository.findRefreshJtiByUserId(1L)).thenReturn(Optional.of("different-jti"));

        assertThatThrownBy(() -> authService.reissue("old-refresh-token"))
                .isInstanceOf(RestApiException.class);
    }

    @Test
    void reissueRejectsSoftDeletedUserAndRemovesServerSession() {
        User deletedUser = socialUserWithId(1L);
        deletedUser.delete();
        Claims oldRefreshClaims = refreshClaims("1", "old-refresh-jti");
        when(jwtProvider.getRefreshTokenClaims("old-refresh-token")).thenReturn(oldRefreshClaims);
        when(redisRepository.findRefreshJtiByUserId(1L)).thenReturn(Optional.of("old-refresh-jti"));
        when(userRepository.findAuthUserById(1L)).thenReturn(Optional.of(deletedUser));

        assertThatThrownBy(() -> authService.reissue("old-refresh-token"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH020")
                );

        verify(redisRepository).deleteRefreshJti(1L);
        verify(jwtProvider, never()).generateToken(any(), any());
    }

    @Test
    void logoutDeletesRefreshSessionResolvedFromAccessToken() {
        Claims accessClaims = accessClaims("1", "access-jti");
        when(jwtProvider.validateAccessTokenAndGetClaims("access-token"))
                .thenReturn(Optional.of(accessClaims));

        authService.logout("access-token", null);

        verify(redisRepository).deleteRefreshJti(1L);
    }

    @Test
    void logoutUsesMatchingRefreshCookieWhenAccessTokenIsUnavailable() {
        Claims refreshClaims = refreshClaims("1", "current-refresh-jti");
        when(jwtProvider.getRefreshTokenClaims("refresh-token")).thenReturn(refreshClaims);
        when(redisRepository.findRefreshJtiByUserId(1L)).thenReturn(Optional.of("current-refresh-jti"));

        authService.logout(null, "refresh-token");

        verify(redisRepository).deleteRefreshJti(1L);
    }

    @Test
    void logoutRevokesAccessAndRefreshSessionsEvenWhenTheyBelongToDifferentUsers() {
        Claims accessClaims = accessClaims("1", "access-jti");
        Claims refreshClaims = refreshClaims("2", "refresh-jti");
        when(jwtProvider.validateAccessTokenAndGetClaims("access-token"))
                .thenReturn(Optional.of(accessClaims));
        when(jwtProvider.getRefreshTokenClaims("refresh-token")).thenReturn(refreshClaims);
        when(redisRepository.findRefreshJtiByUserId(2L)).thenReturn(Optional.of("refresh-jti"));

        authService.logout("access-token", "refresh-token");

        verify(redisRepository).deleteRefreshJti(1L);
        verify(redisRepository).deleteRefreshJti(2L);
    }

    @Test
    void logoutDoesNotDeleteCurrentSessionForStaleRefreshCookie() {
        Claims refreshClaims = refreshClaims("1", "stale-refresh-jti");
        when(jwtProvider.getRefreshTokenClaims("stale-refresh-token")).thenReturn(refreshClaims);
        when(redisRepository.findRefreshJtiByUserId(1L)).thenReturn(Optional.of("current-refresh-jti"));

        authService.logout(null, "stale-refresh-token");

        verify(redisRepository, never()).deleteRefreshJti(any());
    }

    @Test
    void logoutWithoutTokensIsIdempotent() {
        authService.logout(null, null);

        verify(redisRepository, never()).deleteRefreshJti(any());
    }

    @Test
    void logoutKeepsCookieForRetryWhenRevocationCannotBeStored() {
        Claims accessClaims = accessClaims("1", "access-jti");
        when(jwtProvider.validateAccessTokenAndGetClaims("access-token"))
                .thenReturn(Optional.of(accessClaims));
        org.mockito.Mockito.doThrow(new RedisConnectionFailureException("redis unavailable"))
                .when(redisRepository)
                .deleteRefreshJti(1L);
        assertThatThrownBy(() -> authService.logout("access-token", null))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH028")
                );

    }

    @Test
    void reissueKeepsRefreshCookieWhenRedisIsTemporarilyUnavailable() {
        Claims oldRefreshClaims = refreshClaims("1", "old-refresh-jti");
        when(jwtProvider.getRefreshTokenClaims("old-refresh-token")).thenReturn(oldRefreshClaims);
        when(redisRepository.findRefreshJtiByUserId(1L))
                .thenThrow(new RedisConnectionFailureException("redis unavailable"));
        assertThatThrownBy(() -> authService.reissue("old-refresh-token"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH028")
                );

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

}
