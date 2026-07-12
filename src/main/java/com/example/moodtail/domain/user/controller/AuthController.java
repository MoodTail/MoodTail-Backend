package com.example.moodtail.domain.user.controller;

import com.example.moodtail.domain.user.controller.docs.AuthControllerDocs;
import com.example.moodtail.domain.user.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.user.dto.request.LocalLoginRequest;
import com.example.moodtail.domain.user.dto.request.LocalSignupRequest;
import com.example.moodtail.domain.user.dto.request.PasswordChangeRequest;
import com.example.moodtail.domain.user.dto.request.PasswordResetCodeRequest;
import com.example.moodtail.domain.user.dto.request.PasswordResetCodeVerifyRequest;
import com.example.moodtail.domain.user.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.user.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.user.dto.response.LocalAuthResponse;
import com.example.moodtail.domain.user.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.user.dto.response.PasswordResetCodeResponse;
import com.example.moodtail.domain.user.dto.response.PasswordResetVerificationResponse;
import com.example.moodtail.domain.user.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.user.dto.response.TokenResponse;
import com.example.moodtail.domain.user.service.AuthService;
import com.example.moodtail.domain.user.enums.UserRole;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerDocs {

    private static final String NO_STORE = "no-store";

    private final AuthService authService;

    @PostMapping("/guest")
    @Override
    public BaseResponse<GuestLoginResponse> guestLogin(
            @Valid @RequestBody GuestLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.guestLogin(request, httpRequest, response));
    }

    @PostMapping("/oauth-states/{provider}")
    @Override
    public BaseResponse<OAuthStateResponse> createOAuthState(
            @PathVariable String provider,
            @AuthenticationPrincipal PrincipalDetails principal,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.createOAuthState(provider, principal.getUserId()));
    }

    @PostMapping("/login/{provider}")
    @Override
    public BaseResponse<SocialLoginResponse> socialLogin(
            @PathVariable String provider,
            @Valid @RequestBody SocialLoginRequest request,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.socialLogin(provider, request, response));
    }

    @PostMapping("/signup/local")
    @Override
    public BaseResponse<LocalAuthResponse> localSignup(
            @Valid @RequestBody LocalSignupRequest request,
            @AuthenticationPrincipal PrincipalDetails principal,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.localSignup(request, guestUserId(principal), response));
    }

    @PostMapping("/login/local")
    @Override
    public BaseResponse<LocalAuthResponse> localLogin(
            @Valid @RequestBody LocalLoginRequest request,
            @AuthenticationPrincipal PrincipalDetails principal,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.localLogin(request, guestUserId(principal), response));
    }

    @PostMapping("/password-reset/codes")
    @Override
    public BaseResponse<PasswordResetCodeResponse> requestPasswordResetCode(
            @Valid @RequestBody PasswordResetCodeRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.requestPasswordResetCode(request, httpRequest));
    }

    @PostMapping("/password-reset/codes/verify")
    @Override
    public BaseResponse<PasswordResetVerificationResponse> verifyPasswordResetCode(
            @Valid @RequestBody PasswordResetCodeVerifyRequest request,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.verifyPasswordResetCode(request));
    }

    @PatchMapping("/password")
    @Override
    public BaseResponse<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            HttpServletResponse response
    ) {
        preventCaching(response);
        authService.changePassword(request);
        return BaseResponse.onSuccess(null);
    }

    @PostMapping("/reissue")
    @Override
    public BaseResponse<TokenResponse> reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.reissue(request, response));
    }

    @PostMapping("/logout")
    @Override
    public BaseResponse<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        preventCaching(response);
        authService.logout(request, response);
        return BaseResponse.onSuccess(null);
    }

    private void preventCaching(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, NO_STORE);
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
    }

    private Long guestUserId(PrincipalDetails principal) {
        if (principal == null) {
            return null;
        }
        if (UserRole.GUEST.name().equals(principal.getRole())) {
            return principal.getUserId();
        }
        throw new RestApiException(AuthErrorStatus.INVALID_ROLE);
    }
}
