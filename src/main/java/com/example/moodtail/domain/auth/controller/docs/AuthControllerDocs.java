package com.example.moodtail.domain.auth.controller.docs;

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
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Auth", description = "게스트, 로컬 계정, 소셜 계정, 인증 토큰 및 회원탈퇴 API")
public interface AuthControllerDocs {

    String GUEST_LOGIN_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "userId": 101,
                "guestUuid": "550e8400-e29b-41d4-a716-446655440000",
                "isNewUser": true,
                "grantType": "Bearer",
                "accessToken": "guest-access-token"
              }
            }
            """;
    String OAUTH_STATE_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "state": "CFrzH3qHdj5bK5H7S9D0B_H9yYcDJSJt2bf6B8b6T4A",
                "codeChallenge": "bKE9UspwyIPg8LsQHkJaiehiTeUdstI5JZOvaoQRgJA",
                "codeChallengeMethod": "S256",
                "expiresInSeconds": 300
              }
            }
            """;
    String KAKAO_LOGIN_COMPLETED_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "status": "LOGIN_COMPLETED",
                "userId": 37,
                "email": "kakao-user@example.com",
                "nickname": "무드테일",
                "provider": "KAKAO",
                "signupToken": null,
                "signupTokenExpiresInSeconds": null,
                "grantType": "Bearer",
                "accessToken": "member-access-token"
              }
            }
            """;
    String KAKAO_SIGNUP_REQUIRED_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "status": "SIGNUP_REQUIRED",
                "userId": null,
                "email": "kakao-user@example.com",
                "nickname": null,
                "provider": "KAKAO",
                "signupToken": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "signupTokenExpiresInSeconds": 600,
                "grantType": null,
                "accessToken": null
              }
            }
            """;
    String GOOGLE_LOGIN_COMPLETED_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "status": "LOGIN_COMPLETED",
                "userId": 38,
                "email": "google-user@example.com",
                "nickname": "무드테일",
                "provider": "GOOGLE",
                "signupToken": null,
                "signupTokenExpiresInSeconds": null,
                "grantType": "Bearer",
                "accessToken": "member-access-token"
              }
            }
            """;
    String GOOGLE_SIGNUP_REQUIRED_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "status": "SIGNUP_REQUIRED",
                "userId": null,
                "email": "google-user@example.com",
                "nickname": null,
                "provider": "GOOGLE",
                "signupToken": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "signupTokenExpiresInSeconds": 600,
                "grantType": null,
                "accessToken": null
              }
            }
            """;
    String SOCIAL_SIGNUP_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:32:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "status": "SIGNUP_COMPLETED",
                "userId": 38,
                "email": "google-user@example.com",
                "nickname": "무드테일",
                "provider": "GOOGLE",
                "signupToken": null,
                "signupTokenExpiresInSeconds": null,
                "grantType": "Bearer",
                "accessToken": "member-access-token"
              }
            }
            """;
    String SOCIAL_SIGNUP_RECOVERED_LOGIN_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:32:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "status": "LOGIN_COMPLETED",
                "userId": 38,
                "email": "google-user@example.com",
                "nickname": "무드테일",
                "provider": "GOOGLE",
                "signupToken": null,
                "signupTokenExpiresInSeconds": null,
                "grantType": "Bearer",
                "accessToken": "member-access-token"
              }
            }
            """;
    String LOCAL_SIGNUP_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "userId": 39,
                "email": "user@example.com",
                "nickname": "무드테일",
                "isNewUser": true,
                "grantType": "Bearer",
                "accessToken": "member-access-token"
              }
            }
            """;
    String LOCAL_EMAIL_AVAILABLE_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "email": "user@example.com",
                "available": true
              }
            }
            """;
    String LOCAL_EMAIL_UNAVAILABLE_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "email": "joined@example.com",
                "available": false
              }
            }
            """;
    String LOCAL_LOGIN_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "userId": 39,
                "email": "user@example.com",
                "nickname": "무드테일",
                "isNewUser": false,
                "grantType": "Bearer",
                "accessToken": "member-access-token"
              }
            }
            """;
    String PASSWORD_RESET_CODE_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "expiresInSeconds": 300
              }
            }
            """;
    String PASSWORD_RESET_VERIFY_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "resetToken": "GTwWPMLbyR1jm46QKwtnuDFuIBq0YvF4DM6EDdQbcHQ",
                "expiresInSeconds": 600
              }
            }
            """;
    String TOKEN_REISSUE_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "grantType": "Bearer",
                "accessToken": "new-access-token"
              }
            }
            """;
    String VOID_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다."
            }
            """;
    String REFRESH_COOKIE_ISSUED_DESCRIPTION = "Refresh Token 발급 또는 회전 쿠키입니다. 기본 이름은 "
            + "refreshToken이고 Path=/api/v1/auth, HttpOnly가 적용됩니다. Max-Age는 Refresh Token "
            + "유효기간을 따르며, Domain은 설정된 경우에만 포함되고 Secure와 SameSite는 배포 환경 "
            + "설정을 따릅니다. 다른 출처의 프론트엔드는 credentials를 포함해 요청해야 합니다.";
    String SOCIAL_LOGIN_COOKIE_DESCRIPTION = "status가 LOGIN_COMPLETED인 기존 회원에게만 발급됩니다. "
            + REFRESH_COOKIE_ISSUED_DESCRIPTION;
    String REFRESH_COOKIE_CLEARED_DESCRIPTION = "Refresh Token 삭제 쿠키입니다. 발급할 때와 동일한 이름, "
            + "Path, Domain, Secure, SameSite 속성에 Max-Age=0을 적용합니다.";
    String REFRESH_COOKIE_ISSUED_EXAMPLE = "refreshToken=<refresh-token>; Path=/api/v1/auth; "
            + "Max-Age=1209600; HttpOnly; Secure; SameSite=Lax";
    String REFRESH_COOKIE_CLEARED_EXAMPLE = "refreshToken=; Path=/api/v1/auth; "
            + "Max-Age=0; HttpOnly; Secure; SameSite=Lax";

    String COMMON400_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON400",
              "message": "잘못된 요청입니다."
            }
            """;
    String COMMON401_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON401",
              "message": "인증이 필요합니다."
            }
            """;
    String COMMON402_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON402",
              "message": "입력값 검증에 실패했습니다."
            }
            """;
    String COMMON406_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON406",
              "message": "요청 본문 형식이 올바르지 않습니다."
            }
            """;
    String USER400_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "USER400",
              "message": "닉네임 입력값이 올바르지 않습니다."
            }
            """;
    String COMMON500_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "COMMON500",
              "message": "서버 에러가 발생했습니다."
            }
            """;
    String AUTH001_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH001",
              "message": "JWT가 없습니다."
            }
            """;
    String AUTH005_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH005",
              "message": "만료된 리프레시 토큰입니다."
            }
            """;
    String AUTH006_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH006",
              "message": "유효하지 않은 액세스 토큰입니다."
            }
            """;
    String AUTH007_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH007",
              "message": "유효하지 않은 리프레시 토큰입니다."
            }
            """;
    String AUTH009_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH009",
              "message": "권한이 없습니다."
            }
            """;
    String AUTH010_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH010",
              "message": "존재하지 않는 사용자입니다."
            }
            """;
    String AUTH011_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH011",
              "message": "아이디 또는 비밀번호가 올바르지 않습니다."
            }
            """;
    String AUTH012_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH012",
              "message": "이메일 발송 중 오류가 발생했습니다."
            }
            """;
    String AUTH014_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH014",
              "message": "인증 코드가 일치하지 않습니다."
            }
            """;
    String AUTH016_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH016",
              "message": "소셜 로그인 인증 정보가 유효하지 않습니다."
            }
            """;
    String AUTH017_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH017",
              "message": "소셜 로그인 설정이 올바르지 않습니다."
            }
            """;
    String AUTH018_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH018",
              "message": "OAuth state가 만료되었거나 유효하지 않습니다."
            }
            """;
    String AUTH020_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH020",
              "message": "비활성화된 사용자입니다."
            }
            """;
    String AUTH021_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH021",
              "message": "게스트 로그인 요청이 너무 많습니다."
            }
            """;
    String AUTH024_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH024",
              "message": "필수 약관에 모두 동의해야 합니다."
            }
            """;
    String AUTH025_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH025",
              "message": "활성 필수 약관 설정이 올바르지 않습니다."
            }
            """;
    String AUTH026_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH026",
              "message": "약관 동의 정보가 유효하지 않습니다."
            }
            """;
    String AUTH027_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH027",
              "message": "기록을 저장하려면 로그인하세요"
            }
            """;
    String AUTH028_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH028",
              "message": "인증 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요."
            }
            """;
    String AUTH029_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH029",
              "message": "소셜 로그인 제공자를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요."
            }
            """;
    String AUTH030_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH030",
              "message": "소셜 로그인 제공자의 응답을 처리할 수 없습니다."
            }
            """;
    String AUTH031_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH031",
              "message": "OAuth 인증 시작 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
            }
            """;
    String AUTH033_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH033",
              "message": "허용되지 않은 출처의 인증 요청입니다."
            }
            """;
    String AUTH034_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH034",
              "message": "이미 가입된 이메일입니다."
            }
            """;
    String AUTH035_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH035",
              "message": "비밀번호가 보안 정책을 충족하지 않습니다."
            }
            """;
    String AUTH036_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH036",
              "message": "비밀번호 확인이 일치하지 않습니다."
            }
            """;
    String AUTH038_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH038",
              "message": "비밀번호 재설정 서비스를 사용할 수 없습니다."
            }
            """;
    String AUTH039_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH039",
              "message": "비밀번호 재설정 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
            }
            """;
    String AUTH040_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH040",
              "message": "비밀번호 재설정 정보가 만료되었거나 유효하지 않습니다."
            }
            """;
    String AUTH041_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH041",
              "message": "계정 처리는 완료되었지만 로그인 세션을 발급하지 못했습니다. 다시 로그인해주세요."
            }
            """;
    String AUTH042_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH042",
              "message": "비밀번호는 변경되었지만 기존 세션을 종료하지 못했습니다. 보안을 위해 다시 시도해주세요."
            }
            """;
    String AUTH043_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH043",
              "message": "로그인 또는 회원가입 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
            }
            """;
    String AUTH044_EXAMPLE = """
            {
              "timestamp": "2026-07-26T14:30:00",
              "code": "AUTH044",
              "message": "소셜 회원가입 정보가 만료되었거나 유효하지 않습니다."
            }
            """;

    @Operation(
            operationId = "guestLogin",
            summary = "게스트 로그인",
            description = "클라이언트에 저장된 guestUuid로 게스트 사용자를 생성하거나 기존 활성 게스트를 조회합니다. "
                    + "탈퇴한 게스트 계정은 복원하지 않고 같은 guestUuid로 새 게스트 사용자를 생성합니다. "
                    + "성공 시 Access Token을 "
                    + "응답 본문에, Refresh Token을 HttpOnly 쿠키에 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 게스트 로그인 성공",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Set-Cookie", description = REFRESH_COOKIE_ISSUED_DESCRIPTION,
                            schema = @Schema(type = "string", example = REFRESH_COOKIE_ISSUED_EXAMPLE)),
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = GUEST_LOGIN_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = """
                    COMMON402 - guestUuid 누락
                    COMMON406 - guestUuid UUID 역직렬화 또는 요청 본문 JSON 형식 오류
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "429", description = "AUTH021 - 게스트 로그인 요청 한도 초과",
                    content = @Content(examples = @ExampleObject(name = "AUTH021", value = AUTH021_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    @SecurityRequirements
    BaseResponse<GuestLoginResponse> guestLogin(
            GuestLoginRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "createOAuthState",
            summary = "OAuth state 및 PKCE challenge 발급",
            description = "인증 없이 일회성 state와 PKCE S256 challenge를 반환합니다. 프론트엔드는 소셜 "
                    + "인가 요청에 state, code_challenge, code_challenge_method=S256을 전달해야 합니다. "
                    + "state 발급 요청 제한은 클라이언트 주소와 소셜 제공자를 기준으로 적용하며, 발급된 "
                    + "state는 서로 독립적으로 관리됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - OAuth state 발급 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = OAUTH_STATE_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "COMMON400 - 지원하지 않는 소셜 제공자",
                    content = @Content(examples = @ExampleObject(name = "COMMON400", value = COMMON400_EXAMPLE))),
            @ApiResponse(responseCode = "429", description = "AUTH031 - OAuth state 발급 요청 한도 초과",
                    content = @Content(examples = @ExampleObject(name = "AUTH031", value = AUTH031_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = """
                    AUTH017 - 요청한 OAuth 제공자가 비활성 상태이거나 서버 설정이 올바르지 않음
                    COMMON500 - 서버 내부 오류
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH017", value = AUTH017_EXAMPLE),
                            @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE)))
    })
    @SecurityRequirements
    BaseResponse<OAuthStateResponse> createOAuthState(
            @Parameter(
                    description = "소셜 로그인 제공자",
                    schema = @Schema(allowableValues = {"kakao", "google"}),
                    example = "kakao"
            )
            String provider,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "kakaoLogin",
            summary = "카카오 OAuth 인증 및 로그인 분기",
            description = "카카오 인가 코드와 일회성 state를 검증합니다. 기존 계정이면 status가 "
                    + "LOGIN_COMPLETED이고 Access Token과 Refresh Token 쿠키를 발급합니다. 신규 계정이면 "
                    + "status가 SIGNUP_REQUIRED이고 소셜 가입 완료 API에 사용할 10분짜리 signupToken을 "
                    + "반환하며, 이 단계에서는 회원이나 로그인 토큰을 만들지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 기존 로그인 또는 신규 가입 필요",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Set-Cookie", description = SOCIAL_LOGIN_COOKIE_DESCRIPTION,
                            schema = @Schema(type = "string", example = REFRESH_COOKIE_ISSUED_EXAMPLE)),
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "LOGIN_COMPLETED",
                                    summary = "기존 카카오 회원 로그인 완료",
                                    value = KAKAO_LOGIN_COMPLETED_EXAMPLE
                            ),
                            @ExampleObject(
                                    name = "SIGNUP_REQUIRED",
                                    summary = "신규 카카오 회원 가입 정보 입력 필요",
                                    value = KAKAO_SIGNUP_REQUIRED_EXAMPLE
                            )
                    })),
            @ApiResponse(responseCode = "400",
                    description = """
                            COMMON402 - authorizationCode, redirectUri 또는 state 값 검증 실패
                            COMMON406 - 요청 본문 JSON 형식 오류
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401",
                    description = """
                            AUTH016 - 카카오 인가 코드·Redirect URI 또는 필수 프로필 정보가 유효하지 않음
                            AUTH018 - OAuth state가 만료·재사용되었거나 카카오 요청과 일치하지 않음
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH016", value = AUTH016_EXAMPLE),
                            @ExampleObject(name = "AUTH018", value = AUTH018_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403", description = "AUTH020 - 비활성 또는 탈퇴 계정",
                    content = @Content(examples = @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = """
                    AUTH017 - 카카오 OAuth가 비활성 상태이거나 서버 설정이 올바르지 않음
                    COMMON500 - 서버 내부 오류
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH017", value = AUTH017_EXAMPLE),
                            @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "502", description = "AUTH030 - 카카오 응답 처리 실패",
                    content = @Content(examples = @ExampleObject(name = "AUTH030", value = AUTH030_EXAMPLE))),
            @ApiResponse(responseCode = "503",
                    description = """
                            AUTH028 - OAuth state·가입 세션 저장소 또는 인증 DB를 일시적으로 사용할 수 없음
                            AUTH029 - 카카오 OAuth 제공자를 일시적으로 사용할 수 없음
                            AUTH041 - 기존 회원 확인은 끝났으나 로그인 세션 발급에 실패
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE),
                            @ExampleObject(name = "AUTH029", value = AUTH029_EXAMPLE),
                            @ExampleObject(name = "AUTH041", value = AUTH041_EXAMPLE)
                    }))
    })
    @SecurityRequirements
    BaseResponse<SocialLoginResponse> kakaoLogin(
            SocialLoginRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "googleLogin",
            summary = "구글 OAuth 인증 및 로그인 분기",
            description = "구글 인가 코드와 일회성 state를 검증합니다. 기존 계정이면 status가 "
                    + "LOGIN_COMPLETED이고 Access Token과 Refresh Token 쿠키를 발급합니다. 신규 계정이면 "
                    + "status가 SIGNUP_REQUIRED이고 소셜 가입 완료 API에 사용할 10분짜리 signupToken을 "
                    + "반환하며, 이 단계에서는 회원이나 로그인 토큰을 만들지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 기존 로그인 또는 신규 가입 필요",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Set-Cookie", description = SOCIAL_LOGIN_COOKIE_DESCRIPTION,
                            schema = @Schema(type = "string", example = REFRESH_COOKIE_ISSUED_EXAMPLE)),
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "LOGIN_COMPLETED",
                                    summary = "기존 구글 회원 로그인 완료",
                                    value = GOOGLE_LOGIN_COMPLETED_EXAMPLE
                            ),
                            @ExampleObject(
                                    name = "SIGNUP_REQUIRED",
                                    summary = "신규 구글 회원 가입 정보 입력 필요",
                                    value = GOOGLE_SIGNUP_REQUIRED_EXAMPLE
                            )
                    })),
            @ApiResponse(responseCode = "400",
                    description = """
                            COMMON402 - authorizationCode, redirectUri 또는 state 값 검증 실패
                            COMMON406 - 요청 본문 JSON 형식 오류
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401",
                    description = """
                            AUTH016 - 구글 인가 코드·Redirect URI 또는 필수 프로필 정보가 유효하지 않음
                            AUTH018 - OAuth state가 만료·재사용되었거나 구글 요청과 일치하지 않음
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH016", value = AUTH016_EXAMPLE),
                            @ExampleObject(name = "AUTH018", value = AUTH018_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403", description = "AUTH020 - 비활성 또는 탈퇴 계정",
                    content = @Content(examples = @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = """
                    AUTH017 - 구글 OAuth가 비활성 상태이거나 서버 설정이 올바르지 않음
                    COMMON500 - 서버 내부 오류
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH017", value = AUTH017_EXAMPLE),
                            @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "502", description = "AUTH030 - 구글 응답 처리 실패",
                    content = @Content(examples = @ExampleObject(name = "AUTH030", value = AUTH030_EXAMPLE))),
            @ApiResponse(responseCode = "503",
                    description = """
                            AUTH028 - OAuth state·가입 세션 저장소 또는 인증 DB를 일시적으로 사용할 수 없음
                            AUTH029 - 구글 OAuth 제공자를 일시적으로 사용할 수 없음
                            AUTH041 - 기존 회원 확인은 끝났으나 로그인 세션 발급에 실패
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE),
                            @ExampleObject(name = "AUTH029", value = AUTH029_EXAMPLE),
                            @ExampleObject(name = "AUTH041", value = AUTH041_EXAMPLE)
                    }))
    })
    @SecurityRequirements
    BaseResponse<SocialLoginResponse> googleLogin(
            SocialLoginRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "socialSignup",
            summary = "소셜 신규 회원가입 완료",
            description = "OAuth 인증 API가 SIGNUP_REQUIRED로 반환한 일회성 signupToken과 새 UI에서 입력한 "
                    + "닉네임·약관 동의를 제출합니다. 검증이 끝난 뒤에만 회원과 소셜 계정을 생성하고 Access "
                    + "Token과 Refresh Token 쿠키를 발급합니다. 동일한 소셜 계정의 가입이 먼저 완료된 경우 "
                    + "LOGIN_COMPLETED로 기존 계정 로그인을 완료합니다. 닉네임·약관 검증 실패 시에는 같은 "
                    + "signupToken으로 다시 요청할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "COMMON200 - 소셜 회원가입 완료 또는 기존 소셜 계정 로그인 완료",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Set-Cookie", description = REFRESH_COOKIE_ISSUED_DESCRIPTION,
                            schema = @Schema(type = "string", example = REFRESH_COOKIE_ISSUED_EXAMPLE)),
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "SIGNUP_COMPLETED",
                                    value = SOCIAL_SIGNUP_SUCCESS_EXAMPLE
                            ),
                            @ExampleObject(
                                    name = "LOGIN_COMPLETED",
                                    value = SOCIAL_SIGNUP_RECOVERED_LOGIN_EXAMPLE
                            )
                    })),
            @ApiResponse(responseCode = "400",
                    description = """
                            COMMON402 - signupToken, nickname 또는 agreements 값 검증 실패
                            COMMON406 - 요청 본문 JSON 형식 오류
                            USER400 - 앞뒤 공백 제거 후 닉네임 길이가 2~10자를 벗어남
                            AUTH024 - 활성 필수 약관에 모두 동의하지 않음
                            AUTH026 - 중복·비활성·존재하지 않는 약관 ID 등 동의 정보가 유효하지 않음
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE),
                            @ExampleObject(name = "USER400", value = USER400_EXAMPLE),
                            @ExampleObject(name = "AUTH024", value = AUTH024_EXAMPLE),
                            @ExampleObject(name = "AUTH026", value = AUTH026_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401", description = "AUTH044 - 가입 토큰 만료·재사용·오류",
                    content = @Content(examples = @ExampleObject(name = "AUTH044", value = AUTH044_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "AUTH020 - 이미 가입된 비활성 계정",
                    content = @Content(examples = @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = """
                    AUTH025 - 서버의 활성 필수 약관 구성이 올바르지 않음
                    COMMON500 - 서버 내부 오류
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH025", value = AUTH025_EXAMPLE),
                            @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "503", description = """
                    AUTH028 - 가입 세션 저장소 또는 인증 DB를 일시적으로 사용할 수 없음
                    AUTH041 - 회원가입 또는 기존 계정 확인은 끝났으나 로그인 세션 발급에 실패
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE),
                            @ExampleObject(name = "AUTH041", value = AUTH041_EXAMPLE)
                    }))
    })
    @SecurityRequirements
    BaseResponse<SocialLoginResponse> socialSignup(
            SocialSignupRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "localSignup",
            summary = "로컬 계정 회원가입",
            description = "이메일, 비밀번호, 비밀번호 확인, 닉네임과 활성 필수 약관 동의로 로컬 계정을 "
                    + "생성합니다. 비밀번호는 8자 이상이며 UTF-8 "
                    + "기준 72바이트 이하여야 합니다. 성공 시 Access Token은 본문에, Refresh Token은 "
                    + "HttpOnly 쿠키에 발급됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 로컬 회원가입 성공",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Set-Cookie", description = REFRESH_COOKIE_ISSUED_DESCRIPTION,
                            schema = @Schema(type = "string", example = REFRESH_COOKIE_ISSUED_EXAMPLE)),
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = LOCAL_SIGNUP_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400",
                    description = """
                            COMMON402 - 이메일·비밀번호·닉네임·약관 필드 검증 실패
                            COMMON406 - 요청 본문 JSON 형식 오류
                            USER400 - 앞뒤 공백 제거 후 닉네임 길이가 2~10자를 벗어남
                            AUTH024 - 활성 필수 약관에 모두 동의하지 않음
                            AUTH026 - 중복·비활성·존재하지 않는 약관 ID 등 동의 정보가 유효하지 않음
                            AUTH035 - 비밀번호가 길이 또는 영문자·숫자 포함 정책을 충족하지 않음
                            AUTH036 - 비밀번호 확인 값이 일치하지 않음
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE),
                            @ExampleObject(name = "USER400", value = USER400_EXAMPLE),
                            @ExampleObject(name = "AUTH024", value = AUTH024_EXAMPLE),
                            @ExampleObject(name = "AUTH026", value = AUTH026_EXAMPLE),
                            @ExampleObject(name = "AUTH035", value = AUTH035_EXAMPLE),
                            @ExampleObject(name = "AUTH036", value = AUTH036_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "409", description = "AUTH034 - 이미 가입된 이메일",
                    content = @Content(examples = @ExampleObject(name = "AUTH034", value = AUTH034_EXAMPLE))),
            @ApiResponse(responseCode = "429", description = "AUTH043 - 회원가입 요청 한도 초과",
                    content = @Content(examples = @ExampleObject(name = "AUTH043", value = AUTH043_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = """
                    AUTH025 - 서버의 활성 필수 약관 구성이 올바르지 않음
                    COMMON500 - 계정 저장 또는 서버 내부 오류
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH025", value = AUTH025_EXAMPLE),
                            @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "503", description = """
                    AUTH028 - 요청 제한 저장소 또는 인증 DB를 일시적으로 사용할 수 없음
                    AUTH041 - 회원가입은 끝났으나 로그인 세션 발급에 실패
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE),
                            @ExampleObject(name = "AUTH041", value = AUTH041_EXAMPLE)
                    }))
    })
    @SecurityRequirements
    BaseResponse<LocalAuthResponse> localSignup(
            LocalSignupRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "checkLocalEmailAvailability",
            summary = "로컬 회원가입 이메일 중복 확인",
            description = "이메일의 앞뒤 공백을 제거하고 Unicode NFKC·소문자 정규화를 적용한 뒤 로컬 계정 "
                    + "가입 가능 여부를 반환합니다. 중복 이메일도 "
                    + "오류가 아니라 available=false인 COMMON200 응답으로 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 이메일 사용 가능 여부 확인 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = {
                            @ExampleObject(name = "사용 가능", value = LOCAL_EMAIL_AVAILABLE_SUCCESS_EXAMPLE),
                            @ExampleObject(name = "사용 불가", value = LOCAL_EMAIL_UNAVAILABLE_SUCCESS_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "400", description = "COMMON402 - 이메일 누락 또는 형식 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE))),
            @ApiResponse(responseCode = "429", description = "AUTH043 - 이메일 중복 확인 요청 한도 초과",
                    content = @Content(examples = @ExampleObject(name = "AUTH043", value = AUTH043_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    @SecurityRequirements
    BaseResponse<LocalEmailAvailabilityResponse> checkLocalEmailAvailability(
            LocalEmailAvailabilityRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "localLogin",
            summary = "로컬 계정 로그인",
            description = "이메일과 비밀번호로 로그인합니다. 성공 시 Access Token은 본문에, Refresh Token은 "
                    + "HttpOnly 쿠키에 발급됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 로컬 로그인 성공",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Set-Cookie", description = REFRESH_COOKIE_ISSUED_DESCRIPTION,
                            schema = @Schema(type = "string", example = REFRESH_COOKIE_ISSUED_EXAMPLE)),
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = LOCAL_LOGIN_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = """
                    COMMON402 - 이메일 또는 비밀번호 필드 검증 실패
                    COMMON406 - 요청 본문 JSON 형식 오류
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401",
                    description = "AUTH011 - 이메일 또는 비밀번호가 일치하지 않거나 계정이 잠김",
                    content = @Content(examples = @ExampleObject(name = "AUTH011", value = AUTH011_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "AUTH020 - 로컬 계정이 비활성 또는 탈퇴 상태",
                    content = @Content(examples = @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE))),
            @ApiResponse(responseCode = "429", description = "AUTH043 - 로그인 요청 한도 초과",
                    content = @Content(examples = @ExampleObject(name = "AUTH043", value = AUTH043_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = """
                    AUTH028 - 요청 제한 저장소 또는 인증 DB를 일시적으로 사용할 수 없음
                    AUTH041 - 로그인 확인은 끝났으나 회원 세션 발급에 실패
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE),
                            @ExampleObject(name = "AUTH041", value = AUTH041_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    @SecurityRequirements
    BaseResponse<LocalAuthResponse> localLogin(
            LocalLoginRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "requestPasswordResetCode",
            summary = "비밀번호 재설정 인증 코드 발송",
            description = "입력 이메일로 6자리 인증 코드를 발송합니다. 가입 여부 노출을 방지하기 위해 등록되지 "
                    + "않은 이메일도 같은 COMMON200 응답을 반환하지만 메일은 발송하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 인증 코드 발송 요청 처리 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(
                            name = "COMMON200",
                            value = PASSWORD_RESET_CODE_SUCCESS_EXAMPLE
                    ))),
            @ApiResponse(responseCode = "400", description = """
                    COMMON402 - 이메일 누락 또는 형식 오류
                    COMMON406 - 요청 본문 JSON 형식 오류
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "429", description = "AUTH039 - 재발송 대기 또는 요청 한도 초과",
                    content = @Content(examples = @ExampleObject(name = "AUTH039", value = AUTH039_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = """
                    AUTH012 - 인증 코드 이메일 발송 실패
                    COMMON500 - 서버 내부 오류
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH012", value = AUTH012_EXAMPLE),
                            @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "503", description = """
                    AUTH028 - 요청 제한 또는 인증 코드 저장소를 일시적으로 사용할 수 없음
                    AUTH038 - 비밀번호 재설정 기능이 비활성 상태
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE),
                            @ExampleObject(name = "AUTH038", value = AUTH038_EXAMPLE)
                    }))
    })
    @SecurityRequirements
    BaseResponse<PasswordResetCodeResponse> requestPasswordResetCode(
            PasswordResetCodeRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "verifyPasswordResetCode",
            summary = "비밀번호 재설정 인증 코드 확인",
            description = "이메일로 받은 6자리 인증 코드를 확인하고 일회성 resetToken과 유효 시간을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 인증 코드 확인 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(
                            name = "COMMON200",
                            value = PASSWORD_RESET_VERIFY_SUCCESS_EXAMPLE
                    ))),
            @ApiResponse(responseCode = "400", description = """
                    COMMON402 - 이메일 또는 6자리 인증 코드 형식 오류
                    COMMON406 - 요청 본문 JSON 형식 오류
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401", description = "AUTH014 - 인증 코드 불일치·만료 또는 시도 횟수 초과",
                    content = @Content(examples = @ExampleObject(name = "AUTH014", value = AUTH014_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = """
                    AUTH028 - 인증 코드 또는 재설정 토큰 저장소를 일시적으로 사용할 수 없음
                    AUTH038 - 비밀번호 재설정 기능이 비활성 상태
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE),
                            @ExampleObject(name = "AUTH038", value = AUTH038_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    @SecurityRequirements
    BaseResponse<PasswordResetVerificationResponse> verifyPasswordResetCode(
            PasswordResetCodeVerifyRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "changePassword",
            summary = "비밀번호 변경",
            description = "인증 코드 확인 API에서 받은 resetToken으로 비밀번호를 변경하고 기존 인증 세션을 "
                    + "폐기합니다. 비밀번호 저장 후 세션 폐기만 실패하면 변경 완료 상태를 뜻하는 AUTH042를 "
                    + "반환하며, 같은 비밀번호와 resetToken으로 다시 요청해 세션 폐기를 재시도할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 비밀번호 변경 및 기존 세션 폐기 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = VOID_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400",
                    description = """
                            COMMON402 - resetToken 또는 새 비밀번호 필드 검증 실패
                            COMMON406 - 요청 본문 JSON 형식 오류
                            AUTH035 - 새 비밀번호가 길이 또는 영문자·숫자 포함 정책을 충족하지 않음
                            AUTH036 - 새 비밀번호 확인 값이 일치하지 않음
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE),
                            @ExampleObject(name = "AUTH035", value = AUTH035_EXAMPLE),
                            @ExampleObject(name = "AUTH036", value = AUTH036_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401", description = "AUTH040 - resetToken 만료·재사용 또는 무효",
                    content = @Content(examples = @ExampleObject(name = "AUTH040", value = AUTH040_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "AUTH020 - 비활성 또는 탈퇴한 로컬 계정",
                    content = @Content(examples = @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE))),
            @ApiResponse(responseCode = "503",
                    description = """
                            AUTH028 - 비밀번호 재설정 저장소 또는 인증 DB를 일시적으로 사용할 수 없음
                            AUTH038 - 비밀번호 재설정 기능이 비활성 상태
                            AUTH042 - 비밀번호 변경은 완료됐으나 기존 로그인 세션 폐기에 실패
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE),
                            @ExampleObject(name = "AUTH038", value = AUTH038_EXAMPLE),
                            @ExampleObject(name = "AUTH042", value = AUTH042_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    @SecurityRequirements
    BaseResponse<Void> changePassword(
            PasswordChangeRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "reissue",
            summary = "Access Token 재발급",
            description = "HttpOnly Refresh Token 쿠키를 검증하고 Access Token과 Refresh Token을 모두 "
                    + "회전합니다. 성공 시 새 Access Token은 본문에, 새 Refresh Token은 쿠키에 설정합니다. "
                    + "실패 응답에서는 기존 Refresh Token 쿠키를 임의로 삭제하지 않습니다.",
            parameters = {
                    @Parameter(
                            name = "refreshToken",
                            in = ParameterIn.COOKIE,
                            required = true,
                            description = "로그인 또는 회원가입 성공 시 발급된 HttpOnly Refresh Token 쿠키",
                            example = "<refresh-token>"
                    ),
                    @Parameter(
                            name = "Origin",
                            in = ParameterIn.HEADER,
                            required = false,
                            description = "브라우저가 Refresh Token 쿠키와 함께 전송하는 요청 출처. "
                                    + "서버의 CORS 허용 목록 또는 서버 자체 출처여야 합니다.",
                            example = "https://mood-tail.site"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 토큰 재발급 성공",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Set-Cookie", description = REFRESH_COOKIE_ISSUED_DESCRIPTION,
                            schema = @Schema(type = "string", example = REFRESH_COOKIE_ISSUED_EXAMPLE)),
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = TOKEN_REISSUE_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "401",
                    description = """
                            AUTH001 - Refresh Token 쿠키가 없거나 값이 비어 있음
                            AUTH005 - Refresh Token이 만료됨
                            AUTH007 - Refresh Token이 변조·재사용되었거나 현재 세션과 일치하지 않음
                            AUTH010 - Refresh Token의 사용자가 존재하지 않음
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH001", value = AUTH001_EXAMPLE),
                            @ExampleObject(name = "AUTH005", value = AUTH005_EXAMPLE),
                            @ExampleObject(name = "AUTH007", value = AUTH007_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403", description = """
                    AUTH020 - Refresh Token의 사용자가 비활성 또는 탈퇴 상태
                    AUTH033 - Refresh Token 쿠키를 포함한 요청의 Origin이 허용되지 않음
                    """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH033", value = AUTH033_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    @SecurityRequirements
    BaseResponse<TokenResponse> reissue(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "withdraw",
            summary = "회원 탈퇴",
            description = "로그인 회원의 테스트 결과를 포함한 연관 데이터를 하나의 DB 트랜잭션으로 삭제한 뒤 "
                    + "인증 세션과 회원 소유 이미지를 정리합니다. DB 삭제 실패 시 탈퇴되지 않으며 다시 요청할 "
                    + "수 있습니다. DB 삭제 후 저장소 정리 실패는 탈퇴 성공을 유지하고 수동 정리용 오류 로그를 남깁니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 회원 탈퇴 성공",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Set-Cookie", description = REFRESH_COOKIE_CLEARED_DESCRIPTION,
                            schema = @Schema(type = "string", example = REFRESH_COOKIE_CLEARED_EXAMPLE)),
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = VOID_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "401",
                    description = """
                            COMMON401 - Authorization 헤더가 없음
                            AUTH006 - Access Token이 만료·변조되었거나 형식이 올바르지 않음
                            AUTH010 - Access Token의 사용자가 존재하지 않음
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403",
                    description = """
                            AUTH009 - Access Token 역할과 사용자 역할이 불일치하거나 회원 권한이 없음
                            AUTH020 - Access Token의 사용자가 비활성 또는 탈퇴 상태
                            AUTH027 - 게스트 Access Token으로 회원 전용 탈퇴를 요청함
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH027", value = AUTH027_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 회원 데이터 삭제 또는 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    @SecurityRequirement(name = "bearerAuth")
    BaseResponse<Void> withdraw(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            operationId = "logout",
            summary = "로그아웃",
            description = "로컬·소셜·게스트 세션에 공통으로 적용됩니다. 전달된 Access Token과 현재 Refresh "
                    + "Token을 폐기하고 Refresh Token 쿠키를 제거합니다. 토큰이 없거나 이미 폐기된 상태에서 "
                    + "반복 호출해도 COMMON200을 반환합니다.",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            in = ParameterIn.HEADER,
                            required = false,
                            description = "선택 사항. Bearer {accessToken} 형식의 현재 Access Token",
                            example = "Bearer member-access-token"
                    ),
                    @Parameter(
                            name = "refreshToken",
                            in = ParameterIn.COOKIE,
                            required = false,
                            description = "선택 사항. 로그인 또는 회원가입 성공 시 발급된 HttpOnly Refresh Token 쿠키",
                            example = "<refresh-token>"
                    ),
                    @Parameter(
                            name = "Origin",
                            in = ParameterIn.HEADER,
                            required = false,
                            description = "Refresh Token 쿠키를 보내는 브라우저 요청의 출처",
                            example = "https://mood-tail.site"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 로그아웃 성공 또는 이미 로그아웃된 상태",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Set-Cookie", description = REFRESH_COOKIE_CLEARED_DESCRIPTION,
                            schema = @Schema(type = "string", example = REFRESH_COOKIE_CLEARED_EXAMPLE)),
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = VOID_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "AUTH033 - 허용되지 않은 요청 출처",
                    content = @Content(examples = @ExampleObject(name = "AUTH033", value = AUTH033_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    @SecurityRequirements
    BaseResponse<Void> logout(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );
}
