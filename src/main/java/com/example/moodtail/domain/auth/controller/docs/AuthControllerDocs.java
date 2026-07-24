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
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"userId":101,"guestUuid":"550e8400-e29b-41d4-a716-446655440000","isNewUser":true,"grantType":"Bearer","accessToken":"guest-access-token"}}
            """;
    String OAUTH_STATE_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"state":"CFrzH3qHdj5bK5H7S9D0B_H9yYcDJSJt2bf6B8b6T4A","codeChallenge":"PKCE-S256-code-challenge","codeChallengeMethod":"S256","expiresInSeconds":300}}
            """;
    String KAKAO_LOGIN_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"userId":37,"email":"kakao-user@example.com","nickname":"무드테일","provider":"KAKAO","isNewUser":true,"grantType":"Bearer","accessToken":"member-access-token"}}
            """;
    String GOOGLE_LOGIN_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"userId":38,"email":"google-user@example.com","nickname":"무드테일","provider":"GOOGLE","isNewUser":false,"grantType":"Bearer","accessToken":"member-access-token"}}
            """;
    String LOCAL_SIGNUP_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"userId":39,"email":"user@example.com","nickname":"무드테일","isNewUser":true,"grantType":"Bearer","accessToken":"member-access-token"}}
            """;
    String LOCAL_EMAIL_AVAILABLE_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"email":"user@example.com","available":true}}
            """;
    String LOCAL_EMAIL_UNAVAILABLE_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"email":"joined@example.com","available":false}}
            """;
    String LOCAL_LOGIN_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"userId":39,"email":"user@example.com","nickname":"무드테일","isNewUser":false,"grantType":"Bearer","accessToken":"member-access-token"}}
            """;
    String PASSWORD_RESET_CODE_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"expiresInSeconds":300}}
            """;
    String PASSWORD_RESET_VERIFY_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"resetToken":"password-reset-token","expiresInSeconds":600}}
            """;
    String TOKEN_REISSUE_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"grantType":"Bearer","accessToken":"new-access-token"}}
            """;
    String VOID_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON200","message":"요청에 성공했습니다."}
            """;

    String COMMON400_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON400","message":"잘못된 요청입니다."}
            """;
    String COMMON401_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON401","message":"인증이 필요합니다."}
            """;
    String COMMON402_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON402","message":"입력값 검증에 실패했습니다."}
            """;
    String COMMON406_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON406","message":"요청 본문 형식이 올바르지 않습니다."}
            """;
    String COMMON500_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON500","message":"서버 에러가 발생했습니다."}
            """;
    String AUTH001_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH001","message":"JWT가 없습니다."}
            """;
    String AUTH005_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH005","message":"만료된 리프레시 토큰입니다."}
            """;
    String AUTH006_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH006","message":"유효하지 않은 액세스 토큰입니다."}
            """;
    String AUTH007_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH007","message":"유효하지 않은 리프레시 토큰입니다."}
            """;
    String AUTH009_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH009","message":"권한이 없습니다."}
            """;
    String AUTH010_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH010","message":"존재하지 않는 사용자입니다."}
            """;
    String AUTH011_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH011","message":"아이디 또는 비밀번호가 올바르지 않습니다."}
            """;
    String AUTH012_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH012","message":"이메일 발송 중 오류가 발생했습니다."}
            """;
    String AUTH014_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH014","message":"인증 코드가 일치하지 않습니다."}
            """;
    String AUTH016_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH016","message":"소셜 로그인 인증 정보가 유효하지 않습니다."}
            """;
    String AUTH017_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH017","message":"소셜 로그인 설정이 올바르지 않습니다."}
            """;
    String AUTH018_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH018","message":"OAuth state가 만료되었거나 유효하지 않습니다."}
            """;
    String AUTH019_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH019","message":"유효한 게스트 세션이 필요합니다."}
            """;
    String AUTH020_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH020","message":"비활성화된 사용자입니다."}
            """;
    String AUTH021_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH021","message":"게스트 로그인 요청이 너무 많습니다."}
            """;
    String AUTH024_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH024","message":"필수 약관에 모두 동의해야 합니다."}
            """;
    String AUTH025_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH025","message":"활성 필수 약관 설정이 올바르지 않습니다."}
            """;
    String AUTH026_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH026","message":"약관 동의 정보가 유효하지 않습니다."}
            """;
    String AUTH027_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH027","message":"기록을 저장하려면 로그인하세요"}
            """;
    String AUTH028_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH028","message":"인증 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요."}
            """;
    String AUTH029_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH029","message":"소셜 로그인 제공자를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요."}
            """;
    String AUTH030_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH030","message":"소셜 로그인 제공자의 응답을 처리할 수 없습니다."}
            """;
    String AUTH031_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH031","message":"OAuth 인증 시작 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."}
            """;
    String AUTH033_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH033","message":"허용되지 않은 출처의 인증 요청입니다."}
            """;
    String AUTH034_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH034","message":"이미 가입된 이메일입니다."}
            """;
    String AUTH035_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH035","message":"비밀번호가 보안 정책을 충족하지 않습니다."}
            """;
    String AUTH036_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH036","message":"비밀번호 확인이 일치하지 않습니다."}
            """;
    String AUTH038_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH038","message":"비밀번호 재설정 서비스를 사용할 수 없습니다."}
            """;
    String AUTH039_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH039","message":"비밀번호 재설정 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."}
            """;
    String AUTH040_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH040","message":"비밀번호 재설정 정보가 만료되었거나 유효하지 않습니다."}
            """;
    String AUTH041_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH041","message":"계정 처리는 완료되었지만 로그인 세션을 발급하지 못했습니다. 다시 로그인해주세요."}
            """;
    String AUTH042_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH042","message":"비밀번호는 변경되었지만 기존 세션을 종료하지 못했습니다. 보안을 위해 다시 시도해주세요."}
            """;
    String AUTH043_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH043","message":"로그인 또는 회원가입 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."}
            """;

    @Operation(
            operationId = "guestLogin",
            summary = "게스트 로그인",
            description = "클라이언트에 저장된 guestUuid로 게스트 사용자를 생성하거나 기존 활성 게스트를 조회합니다. "
                    + "탈퇴한 게스트의 UUID는 재사용하지 않고 새 게스트로 생성합니다. 성공 시 Access Token을 "
                    + "응답 본문에, Refresh Token을 HttpOnly 쿠키에 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 게스트 로그인 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = GUEST_LOGIN_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "COMMON402/COMMON406 - UUID 누락·형식 오류 또는 잘못된 JSON",
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
            description = "현재 게스트와 제공자에 연결된 일회성 state와 PKCE S256 challenge를 반환합니다. "
                    + "프론트엔드는 소셜 인가 요청에 state, code_challenge, code_challenge_method=S256을 "
                    + "전달해야 합니다. 같은 게스트와 제공자에 새 state를 발급하면 이전 state는 무효화됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - OAuth state 발급 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = OAUTH_STATE_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "COMMON400 - 지원하지 않는 소셜 제공자",
                    content = @Content(examples = @ExampleObject(name = "COMMON400", value = COMMON400_EXAMPLE))),
            @ApiResponse(responseCode = "401",
                    description = "COMMON401/AUTH006/AUTH010/AUTH019 - 인증 토큰 또는 게스트 세션 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE),
                            @ExampleObject(name = "AUTH019", value = AUTH019_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403", description = "AUTH009/AUTH020 - 게스트 권한 또는 사용자 상태 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "429", description = "AUTH031 - OAuth state 발급 요청 한도 초과",
                    content = @Content(examples = @ExampleObject(name = "AUTH031", value = AUTH031_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "AUTH017/COMMON500 - OAuth 설정 또는 서버 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH017", value = AUTH017_EXAMPLE),
                            @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE)))
    })
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
            description = "카카오 인가 코드와 일회성 state를 검증합니다. 기존 카카오 계정이면 로그인하고, "
                    + "미가입 계정이면 닉네임과 활성 필수 약관 동의로 회원가입합니다. 신규 계정은 게스트를 "
                    + "회원으로 전환해 기존 데이터를 유지하며, 기존 계정 로그인은 현재 게스트 세션만 회원 "
                    + "세션으로 교체합니다. 최초 가입일 때만 agreements가 필요합니다. state는 한 번 "
                    + "소비되므로 실패 후 재시도할 때 새 state를 발급받아야 합니다. 성공 시 Access Token은 "
                    + "본문에, Refresh Token은 HttpOnly 쿠키에 발급됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 카카오 회원가입 또는 로그인 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = KAKAO_LOGIN_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400",
                    description = "COMMON402/COMMON406/AUTH024/AUTH026 - 요청 형식·필수 약관 동의 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE),
                            @ExampleObject(name = "AUTH024", value = AUTH024_EXAMPLE),
                            @ExampleObject(name = "AUTH026", value = AUTH026_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401",
                    description = "AUTH016/AUTH018/AUTH019 - 카카오 인증 정보, state 또는 게스트 세션 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH016", value = AUTH016_EXAMPLE),
                            @ExampleObject(name = "AUTH018", value = AUTH018_EXAMPLE),
                            @ExampleObject(name = "AUTH019", value = AUTH019_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403", description = "AUTH020 - 비활성 또는 탈퇴 계정",
                    content = @Content(examples = @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "AUTH017/AUTH025/COMMON500 - OAuth·약관 설정 또는 서버 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH017", value = AUTH017_EXAMPLE),
                            @ExampleObject(name = "AUTH025", value = AUTH025_EXAMPLE),
                            @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "502", description = "AUTH030 - 카카오 응답 처리 실패",
                    content = @Content(examples = @ExampleObject(name = "AUTH030", value = AUTH030_EXAMPLE))),
            @ApiResponse(responseCode = "503",
                    description = "AUTH028/AUTH029/AUTH041 - 인증 인프라·카카오 제공자·세션 발급 오류",
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
            summary = "구글 회원가입 또는 로그인",
            description = "구글 인가 코드와 일회성 state를 검증합니다. 기존 구글 계정이면 로그인하고, "
                    + "미가입 계정이면 닉네임과 활성 필수 약관 동의로 회원가입합니다. 신규 계정은 게스트를 "
                    + "회원으로 전환해 기존 데이터를 유지하며, 기존 계정 로그인은 현재 게스트 세션만 회원 "
                    + "세션으로 교체합니다. 최초 가입일 때만 agreements가 필요합니다. state는 한 번 "
                    + "소비되므로 실패 후 재시도할 때 새 state를 발급받아야 합니다. 성공 시 Access Token은 "
                    + "본문에, Refresh Token은 HttpOnly 쿠키에 발급됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 구글 회원가입 또는 로그인 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = GOOGLE_LOGIN_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400",
                    description = "COMMON402/COMMON406/AUTH024/AUTH026 - 요청 형식·필수 약관 동의 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE),
                            @ExampleObject(name = "AUTH024", value = AUTH024_EXAMPLE),
                            @ExampleObject(name = "AUTH026", value = AUTH026_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401",
                    description = "AUTH016/AUTH018/AUTH019 - 구글 인증 정보, state 또는 게스트 세션 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH016", value = AUTH016_EXAMPLE),
                            @ExampleObject(name = "AUTH018", value = AUTH018_EXAMPLE),
                            @ExampleObject(name = "AUTH019", value = AUTH019_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403", description = "AUTH020 - 비활성 또는 탈퇴 계정",
                    content = @Content(examples = @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "AUTH017/AUTH025/COMMON500 - OAuth·약관 설정 또는 서버 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH017", value = AUTH017_EXAMPLE),
                            @ExampleObject(name = "AUTH025", value = AUTH025_EXAMPLE),
                            @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "502", description = "AUTH030 - 구글 응답 처리 실패",
                    content = @Content(examples = @ExampleObject(name = "AUTH030", value = AUTH030_EXAMPLE))),
            @ApiResponse(responseCode = "503",
                    description = "AUTH028/AUTH029/AUTH041 - 인증 인프라·구글 제공자·세션 발급 오류",
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
            operationId = "localSignup",
            summary = "로컬 계정 회원가입",
            description = "이메일, 비밀번호, 비밀번호 확인, 닉네임과 활성 필수 약관 동의로 로컬 계정을 "
                    + "생성합니다. 게스트 Access Token을 보내면 해당 게스트를 회원으로 전환해 데이터를 "
                    + "유지합니다. 비밀번호는 8자 이상이며 UTF-8 기준 72바이트 이하여야 합니다. 성공 시 "
                    + "Access Token은 본문에, Refresh Token은 HttpOnly 쿠키에 발급됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 로컬 회원가입 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = LOCAL_SIGNUP_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400",
                    description = "COMMON402/COMMON406/AUTH024/AUTH026/AUTH035/AUTH036 - 요청·약관·비밀번호 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE),
                            @ExampleObject(name = "AUTH024", value = AUTH024_EXAMPLE),
                            @ExampleObject(name = "AUTH026", value = AUTH026_EXAMPLE),
                            @ExampleObject(name = "AUTH035", value = AUTH035_EXAMPLE),
                            @ExampleObject(name = "AUTH036", value = AUTH036_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401", description = "AUTH006/AUTH019 - 선택 게스트 인증 정보 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH019", value = AUTH019_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403", description = "AUTH009/AUTH020 - 게스트 권한 또는 사용자 상태 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "409", description = "AUTH034 - 이미 가입된 이메일",
                    content = @Content(examples = @ExampleObject(name = "AUTH034", value = AUTH034_EXAMPLE))),
            @ApiResponse(responseCode = "429", description = "AUTH043 - 회원가입 요청 한도 초과",
                    content = @Content(examples = @ExampleObject(name = "AUTH043", value = AUTH043_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "AUTH025/COMMON500 - 필수 약관 설정 또는 서버 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH025", value = AUTH025_EXAMPLE),
                            @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "503", description = "AUTH028/AUTH041 - 인증 저장소 또는 세션 발급 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE),
                            @ExampleObject(name = "AUTH041", value = AUTH041_EXAMPLE)
                    }))
    })
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
            description = "이메일을 소문자 정규화한 뒤 로컬 계정 가입 가능 여부를 반환합니다. 중복 이메일도 "
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
            description = "이메일과 비밀번호로 로그인합니다. 선택적으로 게스트 Access Token을 보내면 게스트 "
                    + "세션을 종료하고 회원 세션으로 교체합니다. 성공 시 Access Token은 본문에, Refresh "
                    + "Token은 HttpOnly 쿠키에 발급됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 로컬 로그인 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = LOCAL_LOGIN_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "COMMON402/COMMON406 - 이메일·비밀번호 또는 JSON 형식 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401", description = "AUTH006/AUTH011 - 게스트 토큰 또는 로그인 정보 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH011", value = AUTH011_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403", description = "AUTH009/AUTH020 - 게스트 권한 또는 사용자 상태 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "429", description = "AUTH043 - 로그인 요청 한도 초과",
                    content = @Content(examples = @ExampleObject(name = "AUTH043", value = AUTH043_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "AUTH028/AUTH041 - 인증 저장소 또는 세션 발급 오류",
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
            @Parameter(hidden = true) PrincipalDetails principal,
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
            @ApiResponse(responseCode = "400", description = "COMMON402/COMMON406 - 이메일 또는 JSON 형식 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "429", description = "AUTH039 - 재발송 대기 또는 요청 한도 초과",
                    content = @Content(examples = @ExampleObject(name = "AUTH039", value = AUTH039_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "AUTH012/COMMON500 - 이메일 발송 또는 서버 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH012", value = AUTH012_EXAMPLE),
                            @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "503", description = "AUTH028/AUTH038 - 인증 저장소 장애 또는 기능 비활성",
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
            @ApiResponse(responseCode = "400", description = "COMMON402/COMMON406 - 이메일·코드 또는 JSON 형식 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401", description = "AUTH014 - 인증 코드 불일치·만료 또는 시도 횟수 초과",
                    content = @Content(examples = @ExampleObject(name = "AUTH014", value = AUTH014_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "AUTH028/AUTH038 - 인증 저장소 장애 또는 기능 비활성",
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
                    description = "COMMON402/COMMON406/AUTH035/AUTH036 - 요청·비밀번호 정책·확인 값 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE),
                            @ExampleObject(name = "AUTH035", value = AUTH035_EXAMPLE),
                            @ExampleObject(name = "AUTH036", value = AUTH036_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401", description = "AUTH040 - resetToken 만료·재사용 또는 무효",
                    content = @Content(examples = @ExampleObject(name = "AUTH040", value = AUTH040_EXAMPLE))),
            @ApiResponse(responseCode = "503",
                    description = "AUTH028/AUTH038/AUTH042 - 인증 장애, 기능 비활성 또는 세션 폐기 부분 실패",
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
                    + "실패 응답에서는 기존 Refresh Token 쿠키를 임의로 삭제하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 토큰 재발급 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = TOKEN_REISSUE_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "401",
                    description = "AUTH001/AUTH005/AUTH007/AUTH010 - Refresh Token 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH001", value = AUTH001_EXAMPLE),
                            @ExampleObject(name = "AUTH005", value = AUTH005_EXAMPLE),
                            @ExampleObject(name = "AUTH007", value = AUTH007_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403", description = "AUTH020/AUTH033 - 사용자 상태 또는 요청 출처 오류",
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
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = VOID_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "401",
                    description = "COMMON401/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403",
                    description = "AUTH009/AUTH020/AUTH027 - 회원 권한 또는 사용자 상태 오류",
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
                    + "반복 호출해도 COMMON200을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 로그아웃 성공 또는 이미 로그아웃된 상태",
                    useReturnTypeSchema = true,
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
