package com.example.moodtail.domain.auth.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record LocalSignupRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 320, message = "이메일은 320자 이하여야 합니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(max = 72, message = "비밀번호가 너무 깁니다.")
        String password,

        @NotBlank(message = "비밀번호 확인은 필수입니다.")
        @Size(max = 72, message = "비밀번호 확인 값이 너무 깁니다.")
        String passwordConfirm,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
        String nickname,

        @NotEmpty(message = "약관 동의 목록은 필수입니다.")
        @Size(max = 20, message = "약관 동의 항목이 너무 많습니다.")
        List<@NotNull(message = "약관 동의 항목은 null일 수 없습니다.") @Valid TermAgreementRequest> agreements
) {
}
