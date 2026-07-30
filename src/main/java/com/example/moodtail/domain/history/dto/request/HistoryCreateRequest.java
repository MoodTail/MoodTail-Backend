package com.example.moodtail.domain.history.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public record HistoryCreateRequest(
        @NotEmpty
        @Schema(
                description = "마신 칵테일 ID 목록. 같은 칵테일 ID는 중복해서 전달할 수 없습니다.",
                example = "[10, 11]"
        )
        List<@NotNull @Positive Long> cocktailIds,

        @NotNull
        @Schema(description = "음주 기록 날짜", example = "2026-07-05")
        LocalDate recordDate
) {
}
