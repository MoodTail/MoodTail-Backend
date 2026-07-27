package com.example.moodtail.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocalLoginRequest(
        @Schema(description = "로컬 계정 이메일", format = "email", example = "user@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 320, message = "이메일은 320자 이하여야 합니다.")
        String email,

        @Schema(
                description = "로컬 계정 비밀번호. UTF-8 기준 최대 72바이트입니다.",
                example = "moodtail1234",
                accessMode = Schema.AccessMode.WRITE_ONLY
        )
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(max = 72, message = "비밀번호가 너무 깁니다.")
        String password
) {
}
