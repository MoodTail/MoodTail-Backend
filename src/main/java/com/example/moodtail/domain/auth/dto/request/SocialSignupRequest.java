package com.example.moodtail.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SocialSignupRequest(
        @Schema(
                description = "OAuth 인증 API에서 발급한 10분 유효 일회성 가입 토큰",
                example = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        )
        @NotBlank(message = "소셜 회원가입 토큰은 필수입니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9_-]{43}$",
                message = "소셜 회원가입 토큰 형식이 올바르지 않습니다."
        )
        String signupToken,

        @Schema(
                description = "앞뒤 공백 제거 후 Unicode 문자 기준 1~50자인 닉네임",
                minLength = 1,
                maxLength = 50,
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
