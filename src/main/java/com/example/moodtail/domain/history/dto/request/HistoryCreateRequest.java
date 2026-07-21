package com.example.moodtail.domain.history.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record HistoryCreateRequest(
        @NotNull @Positive
        @Schema(description = "마신 칵테일 ID", example = "10")
        Long cocktailId,

        @NotNull
        @Schema(description = "음주 기록 날짜", example = "2026-07-05")
        LocalDate recordDate
) {
}
