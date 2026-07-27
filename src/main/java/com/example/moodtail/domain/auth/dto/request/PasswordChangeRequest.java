package com.example.moodtail.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @Schema(
                description = "비밀번호 재설정 인증 코드 확인 API에서 발급한 일회성 토큰",
                example = "GTwWPMLbyR1jm46QKwtnuDFuIBq0YvF4DM6EDdQbcHQ",
                accessMode = Schema.AccessMode.WRITE_ONLY
        )
        @NotBlank(message = "비밀번호 재설정 토큰은 필수입니다.")
        @Size(max = 128, message = "비밀번호 재설정 토큰이 너무 깁니다.")
        String resetToken,

        @Schema(
                description = "영문자와 숫자를 포함한 8자 이상 새 비밀번호. "
                        + "UTF-8 기준 최대 72바이트입니다.",
                example = "newMoodtail1234",
                accessMode = Schema.AccessMode.WRITE_ONLY
        )
        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Size(max = 72, message = "새 비밀번호가 너무 깁니다.")
        String newPassword,

        @Schema(
                description = "새 비밀번호 확인 값. newPassword와 정확히 일치해야 합니다.",
                example = "newMoodtail1234",
                accessMode = Schema.AccessMode.WRITE_ONLY
        )
        @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
        @Size(max = 72, message = "새 비밀번호 확인 값이 너무 깁니다.")
        String newPasswordConfirm
) {
}
