package com.example.moodtail.domain.history.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Schema(
        description = "음주 기록 수정 요청. cocktailId 또는 recordDate 중 최소 하나를 전달해야 하며, "
                + "두 필드를 모두 생략할 수 없습니다."
)
public record HistoryUpdateRequest(
        @Positive
        @Schema(description = "변경할 칵테일 ID", minimum = "1", example = "11")
        Long cocktailId,

        @Schema(description = "변경할 기록 날짜", example = "2026-07-05")
        LocalDate recordDate
) {
}
