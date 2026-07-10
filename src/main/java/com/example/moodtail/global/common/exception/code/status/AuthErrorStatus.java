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
    EXPIRED_USER_JWT(HttpStatus.UNAUTHORIZED, "AUTH002", "만료된 JWT입니다."),
    UNSUPPORTED_JWT(HttpStatus.UNAUTHORIZED, "AUTH003", "지원하지 않는 JWT입니다."),
    INVALID_ID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH004", "유효하지 않은 ID 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH005", "만료된 리프레시 토큰입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH006", "유효하지 않은 액세스 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH007", "유효하지 않은 리프레시 토큰입니다."),
    FAILED_SOCIAL_LOGIN(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH008", "소셜 로그인에 실패했습니다."),
    INVALID_ROLE(HttpStatus.FORBIDDEN, "AUTH009", "권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH010", "존재하지 않는 사용자입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH011", "아이디 또는 비밀번호가 올바르지 않습니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH012", "이메일 발송 중 오류가 발생했습니다."),
    EMAIL_CODE_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH013", "인증 코드가 만료되었습니다."),
    EMAIL_CODE_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH014", "인증 코드가 일치하지 않습니다."),
    EMAIL_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH015", "이메일 토큰이 만료되었거나 일치하지 않습니다."),
    INVALID_SOCIAL_LOGIN(HttpStatus.UNAUTHORIZED, "AUTH016", "소셜 로그인 인증 정보가 유효하지 않습니다."),
    SOCIAL_LOGIN_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH017", "소셜 로그인 설정이 올바르지 않습니다."),
    INVALID_OAUTH_STATE(HttpStatus.UNAUTHORIZED, "AUTH018", "OAuth state가 만료되었거나 유효하지 않습니다."),
    INVALID_GUEST_SESSION(HttpStatus.UNAUTHORIZED, "AUTH019", "유효한 게스트 세션이 필요합니다."),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "AUTH020", "비활성화된 사용자입니다."),
    TOO_MANY_GUEST_LOGIN_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "AUTH021", "게스트 로그인 요청이 너무 많습니다."),
    SOCIAL_ACCOUNT_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH022", "이미 가입된 소셜 계정입니다."),
    SOCIAL_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH023", "가입되지 않은 소셜 계정입니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "AUTH024", "필수 약관에 모두 동의해야 합니다."),
    TERMS_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH025", "활성 필수 약관 설정이 올바르지 않습니다."),
    INVALID_TERM_AGREEMENT(HttpStatus.BAD_REQUEST, "AUTH026", "약관 동의 정보가 유효하지 않습니다."),
    LOGIN_USER_REQUIRED(HttpStatus.FORBIDDEN, "AUTH027", "로그인 사용자만 이용할 수 있습니다.");

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
