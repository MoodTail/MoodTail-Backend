package com.example.moodtail.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LocalEmailAvailabilityResponse(
        @Schema(description = "정규화된 확인 대상 이메일", format = "email", example = "user@example.com")
        String email,

        @Schema(description = "해당 이메일로 로컬 회원가입할 수 있는지 여부", example = "true")
        boolean available
) {
}
