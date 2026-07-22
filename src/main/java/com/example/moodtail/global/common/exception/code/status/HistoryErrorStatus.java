package com.example.moodtail.global.common.exception.code.status;

import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum HistoryErrorStatus implements BaseCodeInterface {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "HISTORY_400", "히스토리 요청 값이 올바르지 않습니다."),
    DRINKING_RECORD_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "HISTORY_409",
            "해당 날짜에 같은 칵테일 음주 기록이 이미 있습니다."
    ),
    HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "HISTORY_404", "히스토리 기록을 찾을 수 없습니다."),
    TEST_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "HISTORY_TEST_404", "저장된 테스트 결과를 찾을 수 없습니다."),
    PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "HISTORY_PHOTO_404", "히스토리 사진을 찾을 수 없습니다."),
    PHOTO_STORAGE_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "HISTORY_PHOTO_503",
            "사진 저장소를 일시적으로 사용할 수 없습니다."
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
