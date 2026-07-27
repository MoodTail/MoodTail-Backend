package com.example.moodtail.global.common.exception.code.status;

import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ShareErrorStatus implements BaseCodeInterface {
    SHARE_INVALID_RECOMMENDATIONS_SIZE(HttpStatus.BAD_REQUEST, "SHARE_400", "추천 칵테일은 정확히 3개여야 합니다."),
    SHARE_COCKTAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "SHARE_400", "존재하지 않는 칵테일이 포함되어 있습니다."),
    SHARE_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "SHARE404", "존재하지 않는 공유 토큰입니다.");

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
