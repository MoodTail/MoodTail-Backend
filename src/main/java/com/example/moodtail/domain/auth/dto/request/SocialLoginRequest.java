package com.example.moodtail.domain.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SocialLoginRequest(
        @Schema(
                description = "소셜 제공자가 발급한 인가 코드. code라는 필드명도 허용합니다.",
                maxLength = 2048,
                example = "provider-authorization-code"
        )
        @JsonAlias("code")
        @NotBlank(message = "소셜 로그인 인가 코드는 필수입니다.")
        @Size(max = 2048, message = "소셜 로그인 인가 코드가 너무 깁니다.")
        String authorizationCode,

        @Schema(
                description = "소셜 제공자 콘솔과 인가 요청에 등록한 Redirect URI",
                maxLength = 2048,
                example = "https://mood-tail.site/oauth/callback"
        )
        @Size(max = 2048, message = "OAuth redirect URI가 너무 깁니다.")
        String redirectUri,

        @Schema(
                description = "OAuth state 발급 API에서 받은 43자 일회성 state",
                pattern = "^[A-Za-z0-9_-]{43}$",
                example = "CFrzH3qHdj5bK5H7S9D0B_H9yYcDJSJt2bf6B8b6T4A"
        )
        @NotBlank(message = "OAuth state는 필수입니다.")
        @Pattern(regexp = "^[A-Za-z0-9_-]{43}$", message = "OAuth state 형식이 올바르지 않습니다.")
        String state
) {
}
