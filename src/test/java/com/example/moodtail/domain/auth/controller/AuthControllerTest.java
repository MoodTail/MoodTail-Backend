package com.example.moodtail.domain.auth.controller;

import com.example.moodtail.domain.auth.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalSignupRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordChangeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeVerifyRequest;
import com.example.moodtail.domain.auth.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.auth.dto.response.LocalAuthResponse;
import com.example.moodtail.domain.auth.dto.response.LocalEmailAvailabilityResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetCodeResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetVerificationResponse;
import com.example.moodtail.domain.auth.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.auth.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.auth.dto.response.TokenResponse;
import com.example.moodtail.domain.auth.model.AuthResult;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.domain.auth.service.AccountWithdrawalService;
import com.example.moodtail.domain.auth.service.AuthService;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.global.common.exception.ExceptionAdvice;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String VALID_OAUTH_STATE = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    @Mock
    private AuthService authService;

    @Mock
    private AuthHttpSupport authHttpSupport;

    @Mock
    private AccountWithdrawalService accountWithdrawalService;

    private AuthController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authService, accountWithdrawalService, authHttpSupport);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        controller
                )
                .setControllerAdvice(new ExceptionAdvice())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    void socialLoginUsesVersionedProviderEndpointAndCodeAlias() throws Exception {
        SocialLoginResponse socialLoginResponse = new SocialLoginResponse(
                1L,
                "kakao@example.com",
                "카카오유저",
                SocialProvider.KAKAO,
                true,
                "Bearer",
                "access-token"
        );
        when(authService.socialLogin(eq("kakao"), any(SocialLoginRequest.class)))
                .thenReturn(new AuthResult<>(socialLoginResponse, "refresh-token"));

        mockMvc.perform(post("/api/v1/auth/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "kakao-authorization-code",
                                  "state": "%s",
                                  "nickname": "카카오유저",
                                  "agreements": [{"termId": 1, "agreed": true}]
                                }
                                """.formatted(VALID_OAUTH_STATE)))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string(HttpHeaders.PRAGMA, "no-cache"))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.provider").value("KAKAO"))
                .andExpect(jsonPath("$.result.isNewUser").value(true));

        ArgumentCaptor<SocialLoginRequest> requestCaptor = ArgumentCaptor.forClass(SocialLoginRequest.class);
        verify(authService).socialLogin(eq("kakao"), requestCaptor.capture());
        verify(authHttpSupport).setRefreshTokenCookie(any(), eq("refresh-token"));
        org.assertj.core.api.Assertions.assertThat(requestCaptor.getValue().authorizationCode())
                .isEqualTo("kakao-authorization-code");
        org.assertj.core.api.Assertions.assertThat(requestCaptor.getValue().state())
                .isEqualTo(VALID_OAUTH_STATE);
    }

    @Test
    void googleLoginUsesCommonProviderEndpoint() throws Exception {
        SocialLoginResponse socialLoginResponse = new SocialLoginResponse(
                2L,
                "google@example.com",
                "구글유저",
                SocialProvider.GOOGLE,
                false,
                "Bearer",
                "access-token"
        );
        when(authService.socialLogin(eq("google"), any(SocialLoginRequest.class)))
                .thenReturn(new AuthResult<>(socialLoginResponse, "refresh-token"));

        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "google-authorization-code",
                                  "redirectUri": "https://frontend.example.com/oauth/google/callback",
                                  "state": "%s"
                                }
                                """.formatted(VALID_OAUTH_STATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.provider").value("GOOGLE"))
                .andExpect(jsonPath("$.result.email").value("google@example.com"))
                .andExpect(jsonPath("$.result.isNewUser").value(false));

        ArgumentCaptor<SocialLoginRequest> requestCaptor = ArgumentCaptor.forClass(SocialLoginRequest.class);
        verify(authService).socialLogin(eq("google"), requestCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(requestCaptor.getValue().redirectUri())
                .isEqualTo("https://frontend.example.com/oauth/google/callback");
    }

    @Test
    void guestLoginUsesGuestUuidAndReturnsGuestSession() throws Exception {
        GuestLoginResponse guestLoginResponse = new GuestLoginResponse(
                1L,
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                true,
                "Bearer",
                "guest-access-token"
        );
        when(authHttpSupport.clientAddress(any())).thenReturn("203.0.113.7");
        when(authService.guestLogin(any(GuestLoginRequest.class), eq("203.0.113.7")))
                .thenReturn(new AuthResult<>(guestLoginResponse, "guest-refresh-token"));

        mockMvc.perform(post("/api/v1/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "guestUuid": "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.guestUuid").value("b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d"))
                .andExpect(jsonPath("$.result.isNewUser").value(true));
    }

    @Test
    void localSignupCreatesIndependentLocalAccount() throws Exception {
        LocalAuthResponse signupResponse = new LocalAuthResponse(
                2L,
                "user@example.com",
                "무드테일러",
                true,
                "Bearer",
                "signup-access-token"
        );
        when(authHttpSupport.clientAddress(any())).thenReturn("203.0.113.7");
        when(authService.localSignup(any(LocalSignupRequest.class), isNull(), eq("203.0.113.7")))
                .thenReturn(new AuthResult<>(signupResponse, "signup-refresh-token"));

        mockMvc.perform(post("/api/v1/auth/signup/local")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123!",
                                  "passwordConfirm": "password123!",
                                  "nickname": "무드테일러",
                                  "agreements": [
                                    {"termId": 1, "agreed": true}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.userId").value(2L))
                .andExpect(jsonPath("$.result.email").value("user@example.com"))
                .andExpect(jsonPath("$.result.isNewUser").value(true));

        ArgumentCaptor<LocalSignupRequest> requestCaptor = ArgumentCaptor.forClass(LocalSignupRequest.class);
        verify(authService, times(1)).localSignup(
                requestCaptor.capture(),
                isNull(),
                eq("203.0.113.7")
        );
        LocalSignupRequest captured = requestCaptor.getValue();
        assertThat(captured.email()).isEqualTo("user@example.com");
        assertThat(captured.password()).isEqualTo("password123!");
        assertThat(captured.passwordConfirm()).isEqualTo("password123!");
        assertThat(captured.nickname()).isEqualTo("무드테일러");
        assertThat(captured.agreements()).singleElement().satisfies(agreement -> {
            assertThat(agreement.termId()).isEqualTo(1L);
            assertThat(agreement.agreed()).isTrue();
        });
    }

    @Test
    void localSignupRejectsMissingAgreements() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup/local")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123!",
                                  "passwordConfirm": "password123!",
                                  "nickname": "무드테일러",
                                  "agreements": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON402"));
    }

    @Test
    void localSignupRejectsNullAgreementElement() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup/local")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123!",
                                  "passwordConfirm": "password123!",
                                  "nickname": "무드테일러",
                                  "agreements": [null]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON402"));
    }

    @Test
    void localEmailAvailabilityReturnsOnlyNormalizedEmailAndBoolean() throws Exception {
        when(authHttpSupport.clientAddress(any())).thenReturn("203.0.113.7");
        when(authService.checkLocalEmailAvailability("User@Example.com", "203.0.113.7"))
                .thenReturn(new LocalEmailAvailabilityResponse("user@example.com", true));

        mockMvc.perform(get("/api/v1/auth/signup/local/email-availability")
                        .queryParam("email", "User@Example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.email").value("user@example.com"))
                .andExpect(jsonPath("$.result.available").value(true))
                .andExpect(jsonPath("$.result.userId").doesNotExist())
                .andExpect(jsonPath("$.result.provider").doesNotExist());
    }

    @Test
    void localLoginUsesDedicatedEndpoint() throws Exception {
        LocalAuthResponse loginResponse = new LocalAuthResponse(
                2L,
                "user@example.com",
                "무드테일러",
                false,
                "Bearer",
                "login-access-token"
        );
        when(authHttpSupport.clientAddress(any())).thenReturn("203.0.113.7");
        when(authService.localLogin(any(LocalLoginRequest.class), isNull(), eq("203.0.113.7")))
                .thenReturn(new AuthResult<>(loginResponse, "login-refresh-token"));

        mockMvc.perform(post("/api/v1/auth/login/local")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.isNewUser").value(false))
                .andExpect(jsonPath("$.result.accessToken").value("login-access-token"));

        ArgumentCaptor<LocalLoginRequest> requestCaptor = ArgumentCaptor.forClass(LocalLoginRequest.class);
        verify(authService, times(1)).localLogin(
                requestCaptor.capture(),
                isNull(),
                eq("203.0.113.7")
        );
        assertThat(requestCaptor.getValue().email()).isEqualTo("user@example.com");
        assertThat(requestCaptor.getValue().password()).isEqualTo("password123!");
    }

    @Test
    void localLoginRejectsAnInvalidOptionalGuestTokenInsteadOfDroppingGuestData() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login/local")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer expired-or-invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH006"));

        verify(authService, never()).localLogin(any(), any(), any());
    }

    @Test
    void passwordResetFlowExposesCodeVerificationAndPasswordChangeEndpoints() throws Exception {
        when(authHttpSupport.clientAddress(any())).thenReturn("203.0.113.7");
        when(authService.requestPasswordResetCode(any(), eq("203.0.113.7")))
                .thenReturn(new PasswordResetCodeResponse(300L));
        when(authService.verifyPasswordResetCode(any()))
                .thenReturn(new PasswordResetVerificationResponse("reset-token", 600L));

        mockMvc.perform(post("/api/v1/auth/password-reset/codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.expiresInSeconds").value(300));

        mockMvc.perform(post("/api/v1/auth/password-reset/codes/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.resetToken").value("reset-token"));

        mockMvc.perform(patch("/api/v1/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resetToken":"reset-token",
                                  "newPassword":"new-password123!",
                                  "newPasswordConfirm":"new-password123!"
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<PasswordResetCodeRequest> codeRequestCaptor =
                ArgumentCaptor.forClass(PasswordResetCodeRequest.class);
        verify(authService, times(1)).requestPasswordResetCode(
                codeRequestCaptor.capture(),
                eq("203.0.113.7")
        );
        assertThat(codeRequestCaptor.getValue().email()).isEqualTo("user@example.com");

        ArgumentCaptor<PasswordResetCodeVerifyRequest> verifyRequestCaptor =
                ArgumentCaptor.forClass(PasswordResetCodeVerifyRequest.class);
        verify(authService, times(1)).verifyPasswordResetCode(verifyRequestCaptor.capture());
        assertThat(verifyRequestCaptor.getValue().email()).isEqualTo("user@example.com");
        assertThat(verifyRequestCaptor.getValue().code()).isEqualTo("123456");

        ArgumentCaptor<PasswordChangeRequest> changeRequestCaptor =
                ArgumentCaptor.forClass(PasswordChangeRequest.class);
        verify(authService, times(1)).changePassword(changeRequestCaptor.capture());
        assertThat(changeRequestCaptor.getValue().resetToken()).isEqualTo("reset-token");
        assertThat(changeRequestCaptor.getValue().newPassword()).isEqualTo("new-password123!");
        assertThat(changeRequestCaptor.getValue().newPasswordConfirm()).isEqualTo("new-password123!");
    }

    @Test
    void reissueFailureDoesNotSetOrClearRefreshCookie() throws Exception {
        when(authHttpSupport.resolveRefreshToken(any())).thenReturn("old-refresh-token");
        when(authService.reissue("old-refresh-token"))
                .thenThrow(new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN));

        mockMvc.perform(post("/api/v1/auth/reissue"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH007"));

        verify(authHttpSupport, never()).setRefreshTokenCookie(any(), any());
        verify(authHttpSupport, never()).clearRefreshTokenCookie(any());
    }

    @Test
    void reissueSuccessSetsOnlyTheRotatedRefreshCookie() throws Exception {
        when(authHttpSupport.resolveRefreshToken(any())).thenReturn("old-refresh-token");
        when(authService.reissue("old-refresh-token"))
                .thenReturn(new AuthResult<>(
                        new TokenResponse("Bearer", "new-access-token"),
                        "new-refresh-token"
                ));

        mockMvc.perform(post("/api/v1/auth/reissue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.accessToken").value("new-access-token"));

        verify(authHttpSupport).validateCookieAuthenticatedRequest(any());
        verify(authHttpSupport).setRefreshTokenCookie(any(), eq("new-refresh-token"));
        verify(authHttpSupport, never()).clearRefreshTokenCookie(any());
    }

    @Test
    void logoutClearsRefreshCookieAfterServerSessionRevocation() throws Exception {
        when(authHttpSupport.resolveAccessToken(any())).thenReturn("access-token");
        when(authHttpSupport.resolveRefreshToken(any())).thenReturn("refresh-token");

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk());

        verify(authHttpSupport).validateCookieAuthenticatedRequest(any());
        verify(authService).logout("access-token", "refresh-token");
        verify(authHttpSupport).clearRefreshTokenCookie(any());
    }

    @Test
    void logoutFailureKeepsRefreshCookieForRetry() throws Exception {
        when(authHttpSupport.resolveAccessToken(any())).thenReturn("access-token");
        doThrow(new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE))
                .when(authService)
                .logout("access-token", null);

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AUTH028"));

        verify(authHttpSupport, never()).clearRefreshTokenCookie(any());
    }

    @Test
    void withdrawalClearsRefreshCookieOnlyAfterAccountDeletionSucceeds() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.withdraw(new PrincipalDetails(9L, UserRole.USER), response);

        verify(accountWithdrawalService).withdraw(9L);
        verify(authHttpSupport).clearRefreshTokenCookie(response);
    }

    @Test
    void withdrawalFailureKeepsRefreshCookieSoTheMemberCanRetry() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        doThrow(new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE))
                .when(accountWithdrawalService)
                .withdraw(9L);

        assertThatThrownBy(() -> controller.withdraw(
                new PrincipalDetails(9L, UserRole.USER),
                response
        )).isInstanceOf(RestApiException.class);

        verify(authHttpSupport, never()).clearRefreshTokenCookie(response);
    }

    @Test
    void guestLoginRejectsMissingGuestUuid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON402"));
    }

    @Test
    void guestLoginRejectsMalformedGuestUuidWithCommonResponse() throws Exception {
        mockMvc.perform(post("/api/v1/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "guestUuid": "not-a-uuid"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON406"));
    }

    @Test
    void socialLoginRejectsBlankAuthorizationCode() throws Exception {
        mockMvc.perform(post("/api/v1/auth/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "",
                                  "state": "%s"
                                }
                                """.formatted(VALID_OAUTH_STATE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON402"));
    }

    @Test
    void socialLoginRejectsNullAgreementElement() throws Exception {
        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "google-code",
                                  "state": "%s",
                                  "agreements": [null]
                                }
                                """.formatted(VALID_OAUTH_STATE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON402"));
    }
}
