package com.example.moodtail.domain.user.controller;

import com.example.moodtail.domain.user.controller.docs.AuthControllerDocs;
import com.example.moodtail.domain.user.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.user.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.user.dto.request.SocialSignupRequest;
import com.example.moodtail.domain.user.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.user.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.user.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.user.dto.response.SocialSignupResponse;
import com.example.moodtail.domain.user.dto.response.TokenResponse;
import com.example.moodtail.domain.user.service.AuthService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PostMapping("/signup")
    @Override
    public BaseResponse<SocialSignupResponse> socialSignup(
            @Valid @RequestBody SocialSignupRequest request,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.socialSignup(request, response));
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
}
