package com.example.moodtail.global.common.exception.code.status;

import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorStatus implements BaseCodeInterface {
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "USER400", "닉네임 입력값이 올바르지 않습니다."),
    INVALID_PROFILE_UPDATE(HttpStatus.BAD_REQUEST, "USER400", "프로필 수정 요청이 올바르지 않습니다."),
    REPRESENTATIVE_MOOD_TYPE_NOT_UNLOCKED(HttpStatus.BAD_REQUEST, "USER400", "해금한 무드타입만 대표 캐릭터로 지정할 수 있습니다.");

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
