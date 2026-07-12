package com.example.moodtail.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank(message = "비밀번호 재설정 토큰은 필수입니다.")
        @Size(max = 128, message = "비밀번호 재설정 토큰이 너무 깁니다.")
        String resetToken,

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Size(max = 72, message = "새 비밀번호가 너무 깁니다.")
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
        @Size(max = 72, message = "새 비밀번호 확인 값이 너무 깁니다.")
        String newPasswordConfirm
) {
}
