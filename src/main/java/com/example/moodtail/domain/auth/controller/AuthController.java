package com.example.moodtail.domain.auth.controller;

import com.example.moodtail.domain.auth.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalSignupRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordChangeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeVerifyRequest;
import com.example.moodtail.domain.auth.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.auth.dto.response.EmailAvailabilityResponse;
import com.example.moodtail.domain.auth.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.auth.dto.response.LocalAuthResponse;
import com.example.moodtail.domain.auth.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetCodeResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetVerificationResponse;
import com.example.moodtail.domain.auth.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.auth.dto.response.TokenResponse;
import com.example.moodtail.domain.auth.service.AuthService;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "게스트, 로컬 계정, 소셜 계정 및 토큰 API")
public class AuthController {

    private static final String NO_STORE = "no-store";

    private final AuthService authService;

    @PostMapping("/guest")
    @Operation(
            operationId = "guestLogin",
            summary = "게스트 로그인",
            description = "클라이언트에 저장된 게스트 UUID로 사용자를 생성하거나 기존 게스트 세션을 복구합니다."
    )
    @SecurityRequirements
    public BaseResponse<GuestLoginResponse> guestLogin(
            @Valid @RequestBody GuestLoginRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.guestLogin(request, httpRequest, response));
    }

    @PostMapping("/oauth-states/{provider}")
    @Operation(
            operationId = "createOAuthState",
            summary = "OAuth state 발급",
            description = "현재 게스트 사용자와 연결된 일회성 OAuth state와 PKCE S256 challenge를 반환합니다. "
                    + "동일 게스트와 제공자에 새 state를 발급하면 이전 state는 무효화됩니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<OAuthStateResponse> createOAuthState(
            @Parameter(
                    description = "소셜 로그인 제공자",
                    schema = @Schema(allowableValues = {"kakao", "google"}),
                    example = "kakao"
            )
            @PathVariable String provider,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.createOAuthState(provider, requiredGuestUserId(principal)));
    }

    @PostMapping("/login/{provider}")
    @Operation(
            operationId = "socialLogin",
            summary = "소셜 회원가입 또는 로그인",
            description = "인가 코드와 Redis의 일회성 state를 검증합니다. 기존 계정이면 로그인하고, "
                    + "미가입 계정이면 요청의 닉네임과 약관 동의로 같은 호출에서 회원가입합니다. "
                    + "OAuth state를 발급한 게스트의 데이터는 신규 또는 기존 소셜 계정으로 승계됩니다. "
                    + "인가 요청에는 응답받은 PKCE codeChallenge와 S256 방식을 포함해야 합니다."
    )
    @SecurityRequirements
    public BaseResponse<SocialLoginResponse> socialLogin(
            @Parameter(
                    description = "소셜 로그인 제공자",
                    schema = @Schema(allowableValues = {"kakao", "google"}),
                    example = "kakao"
            )
            @PathVariable String provider,
            @Valid @RequestBody SocialLoginRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.socialLogin(provider, request, response));
    }

    @PostMapping("/signup/local")
    @Operation(
            operationId = "localSignup",
            summary = "로컬 계정 회원가입",
            description = "이메일과 비밀번호로 로컬 계정을 생성합니다. 게스트 Access Token이 있으면 기존 게스트 데이터를 승계합니다."
    )
    @SecurityRequirements
    public BaseResponse<LocalAuthResponse> localSignup(
            @Valid @RequestBody LocalSignupRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.localSignup(
                request,
                optionalGuestUserId(principal, httpRequest),
                response
        ));
    }

    @GetMapping("/signup/local/email-availability")
    @Operation(
            operationId = "checkLocalEmailAvailability",
            summary = "로컬 회원가입 이메일 중복 확인",
            description = "정규화된 이메일을 기준으로 사용할 수 있는지 확인합니다. 회원가입 시점에는 DB 고유 제약으로 다시 검증합니다."
    )
    @SecurityRequirements
    public BaseResponse<EmailAvailabilityResponse> checkLocalEmailAvailability(
            @RequestParam
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            @Size(max = 320, message = "이메일은 320자 이하여야 합니다.")
            String email,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.checkLocalEmailAvailability(email));
    }

    @PostMapping("/login/local")
    @Operation(
            operationId = "localLogin",
            summary = "로컬 계정 로그인",
            description = "이메일과 비밀번호로 로그인합니다. 게스트 Access Token이 있으면 게스트 데이터를 기존 계정으로 병합합니다."
    )
    @SecurityRequirements
    public BaseResponse<LocalAuthResponse> localLogin(
            @Valid @RequestBody LocalLoginRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.localLogin(
                request,
                optionalGuestUserId(principal, httpRequest),
                response
        ));
    }

    @PostMapping("/password-reset/codes")
    @Operation(operationId = "requestPasswordResetCode", summary = "비밀번호 찾기 인증 코드 발송")
    @SecurityRequirements
    public BaseResponse<PasswordResetCodeResponse> requestPasswordResetCode(
            @Valid @RequestBody PasswordResetCodeRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.requestPasswordResetCode(request, httpRequest));
    }

    @PostMapping("/password-reset/codes/verify")
    @Operation(operationId = "verifyPasswordResetCode", summary = "비밀번호 찾기 인증 코드 확인")
    @SecurityRequirements
    public BaseResponse<PasswordResetVerificationResponse> verifyPasswordResetCode(
            @Valid @RequestBody PasswordResetCodeVerifyRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.verifyPasswordResetCode(request));
    }

    @PatchMapping("/password")
    @Operation(operationId = "changePassword", summary = "비밀번호 변경")
    @SecurityRequirements
    public BaseResponse<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        preventCaching(response);
        authService.changePassword(request);
        return BaseResponse.onSuccess(null);
    }

    @PostMapping("/reissue")
    @Operation(operationId = "reissue", summary = "토큰 재발급")
    @SecurityRequirements
    public BaseResponse<TokenResponse> reissue(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        preventCaching(response);
        return BaseResponse.onSuccess(authService.reissue(request, response));
    }

    @PostMapping("/logout")
    @Operation(
            operationId = "logout",
            summary = "로그아웃",
            description = "로컬과 소셜 계정에 공통으로 적용됩니다. 액세스 토큰 또는 현재 리프레시 토큰을 "
                    + "폐기하고 인증 쿠키를 제거하며, 반복 호출해도 성공합니다."
    )
    @SecurityRequirements
    public BaseResponse<Void> logout(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        preventCaching(response);
        authService.logout(request, response);
        return BaseResponse.onSuccess(null);
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

    private Long requiredGuestUserId(PrincipalDetails principal) {
        Long guestUserId = principal == null ? null : guestUserId(principal);
        if (guestUserId == null) {
            throw new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION);
        }
        return guestUserId;
    }

    private Long guestUserId(PrincipalDetails principal) {
        if (UserRole.GUEST.name().equals(principal.getRole())) {
            return principal.getUserId();
        }
        throw new RestApiException(AuthErrorStatus.INVALID_ROLE);
    }
}
