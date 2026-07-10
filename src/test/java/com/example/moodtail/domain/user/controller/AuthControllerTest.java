package com.example.moodtail.domain.user.controller;

import com.example.moodtail.domain.user.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.user.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.user.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.user.dto.request.SocialSignupRequest;
import com.example.moodtail.domain.user.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.user.dto.response.SocialSignupResponse;
import com.example.moodtail.domain.user.enums.SocialProvider;
import com.example.moodtail.domain.user.service.AuthService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                                  "state": "oauth-state"
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
    void socialSignupAcceptsProviderOAuthAndAgreements() throws Exception {
        SocialSignupResponse signupResponse = new SocialSignupResponse(
                2L,
                "user@example.com",
                "무드테일러",
                SocialProvider.KAKAO,
                true,
                "Bearer",
                "signup-access-token"
        );
        when(authService.socialSignup(any(SocialSignupRequest.class), any(HttpServletResponse.class)))
                .thenReturn(signupResponse);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "kakao",
                                  "code": "kakao-authorization-code",
                                  "state": "oauth-state",
                                  "nickname": "무드테일러",
                                  "agreements": [
                                    {"termId": 1, "agreed": true}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.userId").value(2L))
                .andExpect(jsonPath("$.result.provider").value("KAKAO"))
                .andExpect(jsonPath("$.result.isNewUser").value(true));
    }

    @Test
    void socialSignupRejectsMissingAgreements() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "kakao",
                                  "code": "kakao-authorization-code",
                                  "state": "oauth-state",
                                  "agreements": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON402"));
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
}
