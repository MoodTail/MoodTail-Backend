package com.example.moodtail.global.common.exception.code.status;

import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorStatus implements BaseCodeInterface {
    EMPTY_JWT(HttpStatus.UNAUTHORIZED, "AUTH001", "JWT가 없습니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH005", "만료된 리프레시 토큰입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH006", "유효하지 않은 액세스 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH007", "유효하지 않은 리프레시 토큰입니다."),
    INVALID_ROLE(HttpStatus.FORBIDDEN, "AUTH009", "권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH010", "존재하지 않는 사용자입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH011", "아이디 또는 비밀번호가 올바르지 않습니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH012", "이메일 발송 중 오류가 발생했습니다."),
    EMAIL_CODE_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH014", "인증 코드가 일치하지 않습니다."),
    INVALID_SOCIAL_LOGIN(HttpStatus.UNAUTHORIZED, "AUTH016", "소셜 로그인 인증 정보가 유효하지 않습니다."),
    SOCIAL_LOGIN_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH017", "소셜 로그인 설정이 올바르지 않습니다."),
    INVALID_OAUTH_STATE(HttpStatus.UNAUTHORIZED, "AUTH018", "OAuth state가 만료되었거나 유효하지 않습니다."),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "AUTH020", "비활성화된 사용자입니다."),
    TOO_MANY_GUEST_LOGIN_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "AUTH021", "게스트 로그인 요청이 너무 많습니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "AUTH024", "필수 약관에 모두 동의해야 합니다."),
    TERMS_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH025", "활성 필수 약관 설정이 올바르지 않습니다."),
    INVALID_TERM_AGREEMENT(HttpStatus.BAD_REQUEST, "AUTH026", "약관 동의 정보가 유효하지 않습니다."),
    LOGIN_USER_REQUIRED(HttpStatus.FORBIDDEN, "AUTH027", "기록을 저장하려면 로그인하세요"),
    AUTH_INFRASTRUCTURE_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "AUTH028",
            "인증 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요."
    ),
    SOCIAL_PROVIDER_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "AUTH029",
            "소셜 로그인 제공자를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요."
    ),
    INVALID_SOCIAL_PROVIDER_RESPONSE(
            HttpStatus.BAD_GATEWAY,
            "AUTH030",
            "소셜 로그인 제공자의 응답을 처리할 수 없습니다."
    ),
    TOO_MANY_OAUTH_STATE_REQUESTS(
            HttpStatus.TOO_MANY_REQUESTS,
            "AUTH031",
            "OAuth 인증 시작 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
    ),
    UNTRUSTED_AUTH_ORIGIN(
            HttpStatus.FORBIDDEN,
            "AUTH033",
            "허용되지 않은 출처의 인증 요청입니다."
    ),
    LOCAL_ACCOUNT_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "AUTH034",
            "이미 가입된 이메일입니다."
    ),
    INVALID_PASSWORD_POLICY(
            HttpStatus.BAD_REQUEST,
            "AUTH035",
            "비밀번호가 보안 정책을 충족하지 않습니다."
    ),
    PASSWORD_CONFIRMATION_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "AUTH036",
            "비밀번호 확인이 일치하지 않습니다."
    ),
    PASSWORD_RESET_DISABLED(
            HttpStatus.SERVICE_UNAVAILABLE,
            "AUTH038",
            "비밀번호 재설정 서비스를 사용할 수 없습니다."
    ),
    TOO_MANY_PASSWORD_RESET_REQUESTS(
            HttpStatus.TOO_MANY_REQUESTS,
            "AUTH039",
            "비밀번호 재설정 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
    ),
    INVALID_PASSWORD_RESET_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH040",
            "비밀번호 재설정 정보가 만료되었거나 유효하지 않습니다."
    ),
    AUTH_SESSION_ISSUE_FAILED(
            HttpStatus.SERVICE_UNAVAILABLE,
            "AUTH041",
            "계정 처리는 완료되었지만 로그인 세션을 발급하지 못했습니다. 다시 로그인해주세요."
    ),
    PASSWORD_CHANGED_SESSION_REVOCATION_FAILED(
            HttpStatus.SERVICE_UNAVAILABLE,
            "AUTH042",
            "비밀번호는 변경되었지만 기존 세션을 종료하지 못했습니다. 보안을 위해 다시 시도해주세요."
    ),
    TOO_MANY_LOCAL_AUTH_REQUESTS(
            HttpStatus.TOO_MANY_REQUESTS,
            "AUTH043",
            "로그인 또는 회원가입 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
    ),
    INVALID_SOCIAL_SIGNUP_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH044",
            "소셜 회원가입 정보가 만료되었거나 유효하지 않습니다."
    );

    private final HttpStatus httpStatus;
    private final boolean isSuccess = false;
    private final String code;
    private final String message;

    @Override
    public BaseCodeDto getCode() {
        return BaseCodeDto.builder()
                .httpStatus(httpStatus)
                .isSuccess(isSuccess)
                .code(code)
                .message(message)
                .build();
    }
}
