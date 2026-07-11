package com.example.moodtail.domain.user.controller.docs;

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
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Auth", description = "게스트, 로컬 계정, 소셜 계정 및 토큰 API")
public interface AuthControllerDocs {

    @Operation(
            operationId = "guestLogin",
            summary = "게스트 로그인",
            description = "클라이언트에 저장된 게스트 UUID로 사용자를 생성하거나 기존 게스트 세션을 복구합니다."
    )
    @SecurityRequirements
    BaseResponse<GuestLoginResponse> guestLogin(
            GuestLoginRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "createOAuthState",
            summary = "OAuth state 발급",
            description = "현재 게스트 사용자와 연결된 일회성 OAuth state와 PKCE S256 challenge를 반환합니다. "
                    + "동일 게스트와 제공자에 새 state를 발급하면 이전 state는 무효화됩니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    BaseResponse<OAuthStateResponse> createOAuthState(
            @Parameter(
                    description = "소셜 로그인 제공자",
                    schema = @Schema(allowableValues = {"kakao", "google"}),
                    example = "kakao"
            )
            String provider,
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "socialLogin",
            summary = "소셜 회원가입 또는 로그인",
            description = "인가 코드와 Redis의 일회성 state를 검증합니다. 기존 계정이면 로그인하고, "
                    + "미가입 계정이면 요청의 닉네임과 약관 동의로 같은 호출에서 회원가입합니다. "
                    + "OAuth state를 발급한 게스트의 데이터는 신규 또는 기존 소셜 계정으로 승계됩니다. "
                    + "인가 요청에는 응답받은 PKCE codeChallenge와 S256 방식을 포함해야 합니다."
    )
    @SecurityRequirements
    BaseResponse<SocialLoginResponse> socialLogin(
            @Parameter(
                    description = "소셜 로그인 제공자",
                    schema = @Schema(allowableValues = {"kakao", "google"}),
                    example = "kakao"
            )
            String provider,
            SocialLoginRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "localSignup",
            summary = "로컬 계정 회원가입",
            description = "이메일과 비밀번호로 로컬 계정을 생성합니다. 게스트 Access Token이 있으면 기존 게스트 데이터를 승계합니다."
    )
    @SecurityRequirements
    BaseResponse<LocalAuthResponse> localSignup(
            LocalSignupRequest request,
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "localLogin",
            summary = "로컬 계정 로그인",
            description = "이메일과 비밀번호로 로그인합니다. 게스트 Access Token이 있으면 게스트 데이터를 기존 계정으로 병합합니다."
    )
    @SecurityRequirements
    BaseResponse<LocalAuthResponse> localLogin(
            LocalLoginRequest request,
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(operationId = "requestPasswordResetCode", summary = "비밀번호 찾기 인증 코드 발송")
    @SecurityRequirements
    BaseResponse<PasswordResetCodeResponse> requestPasswordResetCode(
            PasswordResetCodeRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(operationId = "verifyPasswordResetCode", summary = "비밀번호 찾기 인증 코드 확인")
    @SecurityRequirements
    BaseResponse<PasswordResetVerificationResponse> verifyPasswordResetCode(
            PasswordResetCodeVerifyRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(operationId = "changePassword", summary = "비밀번호 변경")
    @SecurityRequirements
    BaseResponse<Void> changePassword(
            PasswordChangeRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(operationId = "reissue", summary = "토큰 재발급")
    @SecurityRequirements
    BaseResponse<TokenResponse> reissue(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "logout",
            summary = "로그아웃",
            description = "로컬과 소셜 계정에 공통으로 적용됩니다. 액세스 토큰 또는 현재 리프레시 토큰을 "
                    + "폐기하고 인증 쿠키를 제거하며, 반복 호출해도 성공합니다."
    )
    @SecurityRequirements
    BaseResponse<Void> logout(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );
}
