package com.example.moodtail.global.common.exception.code.status;

import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecommendationErrorStatus implements BaseCodeInterface {
    RECOMMENDATION_UNAVAILABLE(HttpStatus.UNPROCESSABLE_ENTITY, "RECOMMENDATION422", "추천 결과를 산출할 수 없습니다."),
    RECOMMENDATION_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "RECOMMENDATION400", "추천 요청 파라미터가 올바르지 않습니다.");

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
