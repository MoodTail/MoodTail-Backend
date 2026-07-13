package com.example.moodtail.global.common.exception.code.status;

import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MoodTestErrorStatus implements BaseCodeInterface {
    MOOD_TEST_QUESTION_QUERY_FAILED(HttpStatus.BAD_REQUEST, "MOOD_TEST400", "테스트 문항 조회에 실패했습니다."),
    MOOD_TEST_INVALID_ANSWER(HttpStatus.BAD_REQUEST, "MOOD_TEST400", "답변 개수 또는 형식이 올바르지 않습니다."),
    MOOD_TEST_QUESTION_OR_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "MOOD_TEST404", "문항 또는 선택지를 찾을 수 없습니다."),
    MOOD_TEST_INVALID_RESULT(HttpStatus.BAD_REQUEST, "MOOD_TEST400", "테스트 결과 저장 요청이 올바르지 않습니다."),
    MOOD_TEST_MOOD_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "MOOD_TEST404", "무드 타입을 찾을 수 없습니다."),
    MOOD_TEST_COCKTAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "MOOD_TEST404", "추천 칵테일을 찾을 수 없습니다."),
    MOOD_TEST_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "MOOD_TEST404", "테스트 결과를 찾을 수 없습니다.");

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
