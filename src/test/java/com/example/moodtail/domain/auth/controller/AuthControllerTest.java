package com.example.moodtail.domain.auth.controller;

import com.example.moodtail.domain.auth.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalSignupRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordChangeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeVerifyRequest;
import com.example.moodtail.domain.auth.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.auth.dto.response.LocalAuthResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetCodeResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetVerificationResponse;
import com.example.moodtail.domain.auth.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.auth.dto.response.SocialLoginResponse;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.domain.auth.service.AuthService;
import com.example.moodtail.global.common.exception.ExceptionAdvice;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
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
        when(authService.socialLogin(eq("kakao"), any(SocialLoginRequest.class), any(HttpServletResponse.class)))
                .thenReturn(socialLoginResponse);

        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "kakao-authorization-code",
                                  "state": "oauth-state",
                                  "nickname": "카카오유저",
                                  "agreements": [{"termId": 1, "agreed": true}]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string(HttpHeaders.PRAGMA, "no-cache"))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.provider").value("KAKAO"))
                .andExpect(jsonPath("$.result.isNewUser").value(true));

        ArgumentCaptor<SocialLoginRequest> requestCaptor = ArgumentCaptor.forClass(SocialLoginRequest.class);
        verify(authService).socialLogin(eq("kakao"), requestCaptor.capture(), any(HttpServletResponse.class));
        org.assertj.core.api.Assertions.assertThat(requestCaptor.getValue().authorizationCode())
                .isEqualTo("kakao-authorization-code");
        org.assertj.core.api.Assertions.assertThat(requestCaptor.getValue().state())
                .isEqualTo("oauth-state");
    }

    @Test
    void oauthStateRejectsMissingGuestPrincipalWithoutNullPointerException() throws Exception {
        mockMvc.perform(post("/api/v1/auth/oauth-states/kakao"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH019"));
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
        when(authService.socialLogin(eq("google"), any(SocialLoginRequest.class), any(HttpServletResponse.class)))
                .thenReturn(socialLoginResponse);

        mockMvc.perform(post("/api/v1/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "google-authorization-code",
                                  "redirectUri": "https://frontend.example.com/oauth/google/callback",
                                  "state": "google-oauth-state"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.provider").value("GOOGLE"))
                .andExpect(jsonPath("$.result.email").value("google@example.com"))
                .andExpect(jsonPath("$.result.isNewUser").value(false));

        ArgumentCaptor<SocialLoginRequest> requestCaptor = ArgumentCaptor.forClass(SocialLoginRequest.class);
        verify(authService).socialLogin(eq("google"), requestCaptor.capture(), any(HttpServletResponse.class));
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
        when(authService.guestLogin(
                any(GuestLoginRequest.class),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        ))
                .thenReturn(guestLoginResponse);

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
        when(authService.localSignup(any(LocalSignupRequest.class), isNull(), any(HttpServletResponse.class)))
                .thenReturn(signupResponse);

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
                any(HttpServletResponse.class)
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
    void localLoginUsesDedicatedEndpoint() throws Exception {
        LocalAuthResponse loginResponse = new LocalAuthResponse(
                2L,
                "user@example.com",
                "무드테일러",
                false,
                "Bearer",
                "login-access-token"
        );
        when(authService.localLogin(any(LocalLoginRequest.class), isNull(), any(HttpServletResponse.class)))
                .thenReturn(loginResponse);

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
                any(HttpServletResponse.class)
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
        when(authService.requestPasswordResetCode(any(), any()))
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
                any(HttpServletRequest.class)
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
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "",
                                  "state": "oauth-state"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON402"));
    }

    @Test
    void socialLoginRejectsNullAgreementElement() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "google-code",
                                  "state": "oauth-state",
                                  "agreements": [null]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON402"));
    }
}
