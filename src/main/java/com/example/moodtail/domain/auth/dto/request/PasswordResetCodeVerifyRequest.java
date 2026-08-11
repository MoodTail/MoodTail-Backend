package com.example.moodtail.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetCodeVerifyRequest(
        @Schema(
                description = "비밀번호 재설정 인증 코드를 요청한 로컬 계정 이메일",
                format = "email",
                example = "user@example.com"
        )
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 320, message = "이메일은 320자 이하여야 합니다.")
        String email,

        @Schema(
                description = "이메일로 받은 6자리 숫자 인증 코드",
                pattern = "^[0-9]{6}$",
                example = "\"123456\""
        )
        @NotBlank(message = "인증 코드는 필수입니다.")
        @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 6자리 숫자여야 합니다.")
        String code
) {
}
