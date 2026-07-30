package com.example.moodtail.domain.auth.controller;

import com.example.moodtail.domain.auth.controller.docs.AuthControllerDocs;
import com.example.moodtail.domain.auth.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalEmailAvailabilityRequest;
import com.example.moodtail.domain.auth.dto.request.LocalLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalSignupRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordChangeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeVerifyRequest;
import com.example.moodtail.domain.auth.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.auth.dto.request.SocialSignupRequest;
import com.example.moodtail.domain.auth.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.auth.dto.response.LocalAuthResponse;
import com.example.moodtail.domain.auth.dto.response.LocalEmailAvailabilityResponse;
import com.example.moodtail.domain.auth.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetCodeResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetVerificationResponse;
import com.example.moodtail.domain.auth.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.auth.dto.response.TokenResponse;
import com.example.moodtail.domain.auth.model.AuthResult;
import com.example.moodtail.domain.auth.service.AccountWithdrawalService;
import com.example.moodtail.domain.auth.service.AuthService;
import com.example.moodtail.domain.user.entity.UserRole;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerDocs {

    private static final String NO_STORE = "no-store";

    private final AuthService authService;
    private final AccountWithdrawalService accountWithdrawalService;
    private final AuthHttpSupport authHttpSupport;

    @Override
    @PostMapping("/guest")
    public BaseResponse<GuestLoginResponse> guestLogin(
            @Valid @RequestBody GuestLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return authenticated(
                authService.guestLogin(request, authHttpSupport.clientAddress(httpRequest)),
                response
        );
    }

    @Override
    @PostMapping("/oauth-states/{provider}")
    public BaseResponse<OAuthStateResponse> createOAuthState(
            @PathVariable String provider,
            @AuthenticationPrincipal PrincipalDetails principal,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.createOAuthState(
                provider,
                optionalGuestUserId(principal, request),
                authHttpSupport.clientAddress(request)
        ));
    }

    @Override
    @PostMapping("/kakao")
    public BaseResponse<SocialLoginResponse> kakaoLogin(
            @Valid @RequestBody SocialLoginRequest request,
            HttpServletResponse response
    ) {
        return socialLogin("kakao", request, response);
    }

    @Override
    @PostMapping("/google")
    public BaseResponse<SocialLoginResponse> googleLogin(
            @Valid @RequestBody SocialLoginRequest request,
            HttpServletResponse response
    ) {
        return socialLogin("google", request, response);
    }

    @Override
    @PostMapping("/signup/social")
    public BaseResponse<SocialLoginResponse> socialSignup(
            @Valid @RequestBody SocialSignupRequest request,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return authenticated(authService.socialSignup(request), response);
    }

    @Override
    @PostMapping("/signup/local")
    public BaseResponse<LocalAuthResponse> localSignup(
            @Valid @RequestBody LocalSignupRequest request,
            @AuthenticationPrincipal PrincipalDetails principal,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return authenticated(
                authService.localSignup(
                        request,
                        optionalGuestUserId(principal, httpRequest),
                        authHttpSupport.clientAddress(httpRequest)
                ),
                response
        );
    }

    @Override
    @GetMapping("/signup/local/email-availability")
    public BaseResponse<LocalEmailAvailabilityResponse> checkLocalEmailAvailability(
            @Valid @ModelAttribute LocalEmailAvailabilityRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.checkLocalEmailAvailability(
                request.email(),
                authHttpSupport.clientAddress(httpRequest)
        ));
    }

    @Override
    @PostMapping("/login/local")
    public BaseResponse<LocalAuthResponse> localLogin(
            @Valid @RequestBody LocalLoginRequest request,
            @AuthenticationPrincipal PrincipalDetails principal,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return authenticated(
                authService.localLogin(
                        request,
                        optionalGuestUserId(principal, httpRequest),
                        authHttpSupport.clientAddress(httpRequest)
                ),
                response
        );
    }

    @Override
    @PostMapping("/password-reset/codes")
    public BaseResponse<PasswordResetCodeResponse> requestPasswordResetCode(
            @Valid @RequestBody PasswordResetCodeRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.requestPasswordResetCode(
                request,
                authHttpSupport.clientAddress(httpRequest)
        ));
    }

    @Override
    @PostMapping("/password-reset/codes/verify")
    public BaseResponse<PasswordResetVerificationResponse> verifyPasswordResetCode(
            @Valid @RequestBody PasswordResetCodeVerifyRequest request,
            HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.verifyPasswordResetCode(request));
    }

    @Override
    @PatchMapping("/password")
    public BaseResponse<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            HttpServletResponse response
    ) {
        preventCaching(response);
        authService.changePassword(request);
        return BaseResponse.onSuccess(null);
    }

    @Override
    @PostMapping("/reissue")
    public BaseResponse<TokenResponse> reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        preventCaching(response);
        authHttpSupport.validateCookieAuthenticatedRequest(request);
        return authenticated(
                authService.reissue(authHttpSupport.resolveRefreshToken(request)),
                response
        );
    }

    @Override
    @DeleteMapping
    public BaseResponse<Void> withdraw(
            @AuthenticationPrincipal PrincipalDetails principal,
            HttpServletResponse response
    ) {
        preventCaching(response);
        accountWithdrawalService.withdraw(principal.getUserId());
        authHttpSupport.clearRefreshTokenCookie(response);
        return BaseResponse.onSuccess(null);
    }

    @Override
    @PostMapping("/logout")
    public BaseResponse<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        preventCaching(response);
        authHttpSupport.validateCookieAuthenticatedRequest(request);
        authService.logout(
                authHttpSupport.resolveAccessToken(request),
                authHttpSupport.resolveRefreshToken(request)
        );
        authHttpSupport.clearRefreshTokenCookie(response);
        return BaseResponse.onSuccess(null);
    }

    private BaseResponse<SocialLoginResponse> socialLogin(
            String provider,
            SocialLoginRequest request,
            HttpServletResponse response
    ) {
        preventCaching(response);
        AuthResult<SocialLoginResponse> result = authService.socialLogin(provider, request);
        if (StringUtils.hasText(result.refreshToken())) {
            authHttpSupport.setRefreshTokenCookie(response, result.refreshToken());
        }
        return BaseResponse.onSuccess(result.response());
    }

    private <T> BaseResponse<T> authenticated(
            AuthResult<T> result,
            HttpServletResponse response
    ) {
        authHttpSupport.setRefreshTokenCookie(response, result.refreshToken());
        return BaseResponse.onSuccess(result.response());
    }

    private void preventCaching(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, NO_STORE);
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
    }

    private Long optionalGuestUserId(PrincipalDetails principal, HttpServletRequest request) {
        if (principal == null) {
            if (request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
                throw new RestApiException(AuthErrorStatus.INVALID_ACCESS_TOKEN);
            }
            return null;
        }
        return guestUserId(principal);
    }

    private Long guestUserId(PrincipalDetails principal) {
        if (UserRole.GUEST.name().equals(principal.getRole())) {
            return principal.getUserId();
        }
        throw new RestApiException(AuthErrorStatus.INVALID_ROLE);
    }
}
