package com.example.moodtail.domain.auth.controller.docs;

import com.example.moodtail.domain.auth.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalEmailAvailabilityRequest;
import com.example.moodtail.domain.auth.dto.request.LocalLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalSignupRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordChangeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeVerifyRequest;
import com.example.moodtail.domain.auth.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.auth.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.auth.dto.response.LocalAuthResponse;
import com.example.moodtail.domain.auth.dto.response.LocalEmailAvailabilityResponse;
import com.example.moodtail.domain.auth.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetCodeResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetVerificationResponse;
import com.example.moodtail.domain.auth.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.auth.dto.response.TokenResponse;
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
            operationId = "kakaoLogin",
            summary = "카카오 회원가입 또는 로그인",
            description = "인가 코드와 Redis의 일회성 state를 검증합니다. 기존 계정이면 로그인하고, "
                    + "미가입 계정이면 요청의 닉네임과 약관 동의로 같은 호출에서 회원가입합니다. "
                    + "신규 계정은 게스트 사용자를 회원으로 전환해 기존 데이터를 유지합니다. "
                    + "이미 연결된 계정으로 로그인하면 게스트 데이터를 기존 회원에게 승계한 뒤 "
                    + "게스트 세션을 회원 세션으로 교체합니다. "
                    + "최초 가입일 때만 agreements가 필요합니다. 인가 요청에는 응답받은 PKCE "
                    + "codeChallenge와 S256 방식을 포함해야 합니다. state는 한 번 소비되므로 "
                    + "외부 제공자 오류가 발생하면 현재 게스트 세션에서 새 state를 발급받아야 합니다. "
                    + "계정 처리 후 세션 발급만 실패하면 AUTH041을 반환하며 다시 로그인하면 됩니다."
    )
    @SecurityRequirements
    BaseResponse<SocialLoginResponse> kakaoLogin(
            SocialLoginRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "googleLogin",
            summary = "구글 회원가입 또는 로그인",
            description = "인가 코드와 Redis의 일회성 state를 검증합니다. 기존 계정이면 로그인하고, "
                    + "미가입 계정이면 요청의 닉네임과 약관 동의로 같은 호출에서 회원가입합니다. "
                    + "신규 계정은 게스트 사용자를 회원으로 전환해 기존 데이터를 유지합니다. "
                    + "이미 연결된 계정으로 로그인하면 게스트 데이터를 기존 회원에게 승계한 뒤 "
                    + "게스트 세션을 회원 세션으로 교체합니다. "
                    + "최초 가입일 때만 agreements가 필요합니다. 인가 요청에는 응답받은 PKCE "
                    + "codeChallenge와 S256 방식을 포함해야 합니다. state는 한 번 소비되므로 "
                    + "외부 제공자 오류가 발생하면 현재 게스트 세션에서 새 state를 발급받아야 합니다. "
                    + "계정 처리 후 세션 발급만 실패하면 AUTH041을 반환하며 다시 로그인하면 됩니다."
    )
    @SecurityRequirements
    BaseResponse<SocialLoginResponse> googleLogin(
            SocialLoginRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "localSignup",
            summary = "로컬 계정 회원가입",
            description = "이메일, 비밀번호, 닉네임과 필수 약관 동의로 로컬 계정을 생성합니다. "
                    + "게스트 Access Token이 있으면 해당 게스트 계정을 회원 계정으로 전환합니다. "
                    + "가입 후 세션 발급만 실패하면 AUTH041을 반환하며 로컬 로그인을 시도하면 됩니다."
    )
    @SecurityRequirements
    BaseResponse<LocalAuthResponse> localSignup(
            LocalSignupRequest request,
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "checkLocalEmailAvailability",
            summary = "로컬 회원가입 이메일 중복 확인",
            description = "정규화된 이메일과 사용 가능 여부만 반환합니다."
    )
    @SecurityRequirements
    BaseResponse<LocalEmailAvailabilityResponse> checkLocalEmailAvailability(
            LocalEmailAvailabilityRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "localLogin",
            summary = "로컬 계정 로그인",
            description = "이메일과 비밀번호로 로그인합니다. 게스트 Access Token이 있으면 게스트 세션을 "
                    + "종료하고 회원 세션으로 교체합니다. 세션 발급에 실패하면 AUTH041을 반환합니다."
    )
    @SecurityRequirements
    BaseResponse<LocalAuthResponse> localLogin(
            LocalLoginRequest request,
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
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

    @Operation(
            operationId = "changePassword",
            summary = "비밀번호 변경",
            description = "비밀번호 저장 후 기존 세션 폐기만 실패하면 비밀번호는 이미 변경된 상태임을 "
                    + "나타내는 AUTH042를 반환합니다. 같은 비밀번호로 다시 요청하면 세션 폐기만 재시도합니다."
    )
    @SecurityRequirements
    BaseResponse<Void> changePassword(
            PasswordChangeRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "reissue",
            summary = "토큰 재발급",
            description = "재발급 성공 시에만 새 리프레시 쿠키를 설정하며 실패 응답에서는 기존 쿠키를 삭제하지 않습니다."
    )
    @SecurityRequirements
    BaseResponse<TokenResponse> reissue(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "withdraw",
            summary = "회원 탈퇴",
            description = "테스트 결과를 포함한 회원 연관 데이터 삭제를 먼저 완료한 뒤 인증 세션과 "
                    + "회원 소유 이미지를 정리합니다. "
                    + "저장소 삭제 실패는 수동 정리가 가능하도록 오류 로그로 남깁니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    BaseResponse<Void> withdraw(
            @Parameter(hidden = true) PrincipalDetails principal,
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
