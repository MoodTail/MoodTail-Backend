package com.example.moodtail.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordResetCodeResponse(
        @Schema(description = "인증 코드 유효 시간(초)", example = "300")
        long expiresInSeconds
) {
}
