package com.example.moodtail.domain.user.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TermAgreementRequest(
        @NotNull(message = "약관 ID는 필수입니다.")
        @Positive(message = "약관 ID는 양수여야 합니다.")
        Long termId,

        @NotNull(message = "약관 동의 여부는 필수입니다.")
        Boolean agreed
) {
}
