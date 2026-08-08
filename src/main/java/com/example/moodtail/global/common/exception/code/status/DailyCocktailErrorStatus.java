package com.example.moodtail.global.common.exception.code.status;

import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DailyCocktailErrorStatus implements BaseCodeInterface{
    DAILY_COCKTAIL_NOT_FOUND(HttpStatus.NOT_FOUND,"DAILY_COCKTAIL404","해당 지역의 오늘의 칵테일을 찾을 수 없습니다."),
    DAILY_COCKTAIL_CACHE_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"DAILY_COCKTAIL500","오늘의 칵테일 캐시 데이터를 처리할 수 없습니다."),
    DAILY_COCKTAIL_CACHE_DESERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DAILY_COCKTAIL500_1", "오늘의 칵테일 캐시 데이터를 읽을 수 없습니다."),
    DAILY_COCKTAIL_REDIS_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE,"DAILY_COCKTAIL503","오늘의 칵테일 저장소를 일시적으로 사용할 수 없습니다.");

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
