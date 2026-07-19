package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalSignupRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordChangeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeVerifyRequest;
import com.example.moodtail.domain.auth.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.auth.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.auth.dto.response.LocalAuthResponse;
import com.example.moodtail.domain.auth.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetCodeResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetVerificationResponse;
import com.example.moodtail.domain.auth.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.auth.dto.response.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    GuestLoginResponse guestLogin(
            GuestLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    );

    OAuthStateResponse createOAuthState(String providerName, Long guestUserId);

    SocialLoginResponse socialLogin(
            String providerName,
            SocialLoginRequest request,
            HttpServletResponse response
    );

    LocalAuthResponse localSignup(
            LocalSignupRequest request,
            Long guestUserId,
            HttpServletResponse response
    );

    LocalAuthResponse localLogin(
            LocalLoginRequest request,
            Long guestUserId,
            HttpServletResponse response
    );

    PasswordResetCodeResponse requestPasswordResetCode(
            PasswordResetCodeRequest request,
            HttpServletRequest httpRequest
    );

    PasswordResetVerificationResponse verifyPasswordResetCode(PasswordResetCodeVerifyRequest request);

    void changePassword(PasswordChangeRequest request);

    TokenResponse reissue(HttpServletRequest request, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);
}
