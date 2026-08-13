package com.example.moodtail.global.common.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"timestamp", "code", "message", "result"})
@Schema(description = "MoodTail 공통 JSON 응답. result가 null이면 result 필드는 생략됩니다.")
public class BaseResponse<T> {

    @Schema(
            description = "서버가 응답을 생성한 시각",
            type = "string",
            format = "date-time",
            example = "2026-08-13T14:30:00"
    )
    private final LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "애플리케이션 응답 코드", example = "COMMON200")
    private final String code;

    @Schema(description = "응답 코드에 대응하는 사용자 메시지", example = "요청에 성공했습니다.")
    private final String message;

    @Schema(description = "성공 데이터 또는 필드별 검증 오류. 값이 null이면 응답에서 생략됩니다.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    public static <T> BaseResponse<T> onSuccess(T result) {
        return new BaseResponse<>("COMMON200", "요청에 성공했습니다.", result);
    }

    public static <T> BaseResponse<T> onFailure(String code, String message, T data) {
        return new BaseResponse<>(code, message, data);
    }
}
