package com.example.moodtail.domain.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SocialLoginRequest(
        @JsonAlias("code")
        @NotBlank(message = "소셜 로그인 인가 코드는 필수입니다.")
        @Size(max = 2048, message = "소셜 로그인 인가 코드가 너무 깁니다.")
        String authorizationCode,

        @Size(max = 2048, message = "OAuth redirect URI가 너무 깁니다.")
        String redirectUri,

        @NotBlank(message = "OAuth state는 필수입니다.")
        @Pattern(regexp = "^[A-Za-z0-9_-]{43}$", message = "OAuth state 형식이 올바르지 않습니다.")
        String state
) {
}
