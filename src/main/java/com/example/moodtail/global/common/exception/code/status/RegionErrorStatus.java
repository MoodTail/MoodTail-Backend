package com.example.moodtail.global.common.exception.code.status;

import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RegionErrorStatus implements BaseCodeInterface {

    REGION_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "REGION400", "지원하지 않는 지역입니다."),
    REGION_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "REGION500", "지역 API 설정이 올바르지 않습니다."),
    INVALID_REGION_RESPONSE(HttpStatus.BAD_GATEWAY, "REGION502", "지역 서비스의 응답을 처리할 수 없습니다."),
    REGION_PROVIDER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "REGION503", "지역 서비스를 일시적으로 사용할 수 없습니다."),
    REGION_RATE_LIMIT_EXCEEDED(HttpStatus.SERVICE_UNAVAILABLE, "REGION503_1", "지역 API 호출 한도를 초과했습니다.");

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
