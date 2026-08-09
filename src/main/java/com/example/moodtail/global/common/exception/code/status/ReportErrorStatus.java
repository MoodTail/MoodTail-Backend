package com.example.moodtail.global.common.exception.code.status;

import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReportErrorStatus implements BaseCodeInterface {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "REPORT400", "월간 리포트 요청 값이 올바르지 않습니다."),
    SHARE_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT404", "공유된 월간 리포트를 찾을 수 없습니다."),
    INSUFFICIENT_DATA(HttpStatus.CONFLICT, "REPORT409", "월간 리포트 생성에 필요한 데이터가 부족합니다."),
    SHARE_IMAGE_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "REPORT_IMAGE503",
            "월간 리포트 공유 이미지를 일시적으로 저장할 수 없습니다."
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
