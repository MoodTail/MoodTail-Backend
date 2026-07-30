package com.example.moodtail.global.common.exception.code.status;

import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum WeatherErrorStatus implements BaseCodeInterface {
    WEATHER_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "WEATHER500", "날씨 API 설정이 올바르지 않습니다."),
    WEATHER_PROVIDER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "WEATHER503", "날씨 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해 주세요."),
    WEATHER_RATE_LIMIT_EXCEEDED(HttpStatus.SERVICE_UNAVAILABLE, "WEATHER503", "날씨 API 호출 한도를 초과했습니다. 잠시 후 다시 시도해 주세요."),
    INVALID_WEATHER_RESPONSE(HttpStatus.BAD_GATEWAY, "WEATHER502", "날씨 서비스의 응답을 처리할 수 없습니다.");

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
