package com.example.moodtail.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TermAgreementRequest(
        @Schema(description = "약관 조회 API가 반환한 양수 약관 ID", minimum = "1", example = "1")
        @NotNull(message = "약관 ID는 필수입니다.")
        @Positive(message = "약관 ID는 양수여야 합니다.")
        Long termId,

        @Schema(description = "해당 약관 동의 여부", example = "true")
        @NotNull(message = "약관 동의 여부는 필수입니다.")
        Boolean agreed
) {
}
