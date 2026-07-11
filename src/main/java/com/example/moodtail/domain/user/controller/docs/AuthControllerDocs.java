package com.example.moodtail.domain.user.controller.docs;

import com.example.moodtail.domain.user.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.user.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.user.dto.request.SocialSignupRequest;
import com.example.moodtail.domain.user.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.user.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.user.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.user.dto.response.SocialSignupResponse;
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

@Tag(name = "Auth", description = "소셜 회원가입, 로그인, 토큰 재발급, 로그아웃 API")
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
            description = "현재 게스트 사용자와 연결된 일회성 OAuth state를 Redis에 저장하고 반환합니다."
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
            summary = "소셜 로그인",
            description = "인가 코드와 Redis의 일회성 state를 검증하고 가입된 소셜 계정으로 로그인합니다. "
                    + "Google 인가 요청에는 openid, profile, email scope를 사용합니다."
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
            operationId = "socialSignup",
            summary = "소셜 회원가입",
            description = "카카오 또는 구글 OAuth 인증 후 게스트 사용자를 정회원으로 전환하고 약관 동의를 저장합니다. "
                    + "Google 인가 요청에는 openid, profile, email scope를 사용합니다."
    )
    @SecurityRequirements
    BaseResponse<SocialSignupResponse> socialSignup(
            SocialSignupRequest request,
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
            description = "액세스 토큰 또는 현재 리프레시 토큰을 폐기하고 인증 쿠키를 제거합니다. 반복 호출해도 성공합니다."
    )
    @SecurityRequirements
    BaseResponse<Void> logout(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );
}
