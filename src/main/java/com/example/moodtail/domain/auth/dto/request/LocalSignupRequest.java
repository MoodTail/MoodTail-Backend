package com.example.moodtail.domain.auth.dto.request;

import com.example.moodtail.domain.user.validator.NicknameValidator;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record LocalSignupRequest(
        @Schema(description = "가입할 로컬 계정 이메일", format = "email", example = "user@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 320, message = "이메일은 320자 이하여야 합니다.")
        String email,

        @Schema(
                description = "영문자와 숫자를 포함한 8자 이상 비밀번호. "
                        + "UTF-8 기준 최대 72바이트입니다.",
                example = "moodtail1234",
                accessMode = Schema.AccessMode.WRITE_ONLY
        )
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(max = 72, message = "비밀번호가 너무 깁니다.")
        String password,

        @Schema(
                description = "비밀번호 확인 값. password와 정확히 일치해야 합니다.",
                example = "moodtail1234",
                accessMode = Schema.AccessMode.WRITE_ONLY
        )
        @NotBlank(message = "비밀번호 확인은 필수입니다.")
        @Size(max = 72, message = "비밀번호 확인 값이 너무 깁니다.")
        String passwordConfirm,

        @Schema(
                description = NicknameValidator.POLICY_DESCRIPTION,
                minLength = NicknameValidator.MIN_LENGTH,
                maxLength = NicknameValidator.MAX_LENGTH,
                example = "무드테일"
        )
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        @ArraySchema(
                minItems = 1,
                maxItems = 20,
                arraySchema = @Schema(
                        description = "현재 활성 약관에 대한 동의 목록. "
                                + "모든 필수 약관 동의가 포함되어야 합니다.",
                        example = "[{\"termId\":1,\"agreed\":true},{\"termId\":2,\"agreed\":true}]"
                )
        )
        @NotEmpty(message = "약관 동의 목록은 필수입니다.")
        @Size(min = 1, max = 20, message = "약관 동의 항목은 1개 이상 20개 이하여야 합니다.")
        List<
                @NotNull(message = "약관 동의 항목은 null일 수 없습니다.")
                @Valid TermAgreementRequest
        > agreements
) {
}
