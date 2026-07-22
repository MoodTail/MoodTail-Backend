package com.example.moodtail.global.common.exception.code.status;

import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CollectionErrorStatus implements BaseCodeInterface {

    COLLECTION_REPRESENTATIVE_MOOD_TYPE_NOT_UNLOCKED(HttpStatus.BAD_REQUEST, "COLLECTION_MOOD_TYPE400", "해금한 무드 타입만 대표 타입으로 지정할 수 있습니다."),
    COLLECTION_SHARE_NOT_FOUND(HttpStatus.NOT_FOUND, "COLLECTION_SHARE404", "공유된 도감을 찾을 수 없습니다.");

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
