package com.example.moodtail.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordResetVerificationResponse(
        @Schema(
                description = "비밀번호 변경 API에 전달할 일회성 재설정 토큰",
                example = "GTwWPMLbyR1jm46QKwtnuDFuIBq0YvF4DM6EDdQbcHQ"
        )
        String resetToken,

        @Schema(description = "재설정 토큰 유효 시간(초)", example = "600")
        long expiresInSeconds
) {
}
