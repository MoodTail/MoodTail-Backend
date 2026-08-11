package com.example.moodtail.domain.history.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record HistoryCreateRequest(
        @ArraySchema(
                minItems = 1,
                uniqueItems = true,
                schema = @Schema(minimum = "1"),
                arraySchema = @Schema(
                        description = "마신 칵테일 ID 목록. "
                                + "같은 칵테일 ID는 중복해서 전달할 수 없습니다.",
                        example = "[10, 11]"
                )
        )
        @NotEmpty
        @Size(min = 1, message = "칵테일 ID 목록은 한 개 이상이어야 합니다.")
        List<@NotNull @Positive Long> cocktailIds,

        @NotNull
        @Schema(description = "음주 기록 날짜", example = "2026-07-05")
        LocalDate recordDate
) {
}
