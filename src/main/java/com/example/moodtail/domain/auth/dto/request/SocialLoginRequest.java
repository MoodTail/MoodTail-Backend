package com.example.moodtail.domain.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SocialLoginRequest(
        @JsonAlias("code")
        @NotBlank(message = "소셜 로그인 인가 코드는 필수입니다.")
        @Size(max = 2048, message = "소셜 로그인 인가 코드가 너무 깁니다.")
        String authorizationCode,

        @Size(max = 2048, message = "OAuth redirect URI가 너무 깁니다.")
        String redirectUri,

        @NotBlank(message = "OAuth state는 필수입니다.")
        @Pattern(regexp = "^[A-Za-z0-9_-]{43}$", message = "OAuth state 형식이 올바르지 않습니다.")
        String state,

        String nickname,

        @Schema(description = "기존 회원 로그인 시 생략 가능하며, 최초 소셜 가입 시 필수 약관 동의가 필요합니다.")
        @Size(max = 20, message = "약관 동의 항목이 너무 많습니다.")
        List<@NotNull(message = "약관 동의 항목은 null일 수 없습니다.") @Valid TermAgreementRequest> agreements
) {

    public SocialLoginRequest(String authorizationCode, String redirectUri, String state) {
        this(authorizationCode, redirectUri, state, null, null);
    }
}
