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
    LOGIN_USER_REQUIRED(HttpStatus.FORBIDDEN, "AUTH016", "로그인 사용자만 이용할 수 있습니다.");

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
