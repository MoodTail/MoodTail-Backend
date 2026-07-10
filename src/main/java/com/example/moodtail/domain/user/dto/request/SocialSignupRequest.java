package com.example.moodtail.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SocialSignupRequest(
        @NotBlank(message = "소셜 로그인 제공자는 필수입니다.")
        @Size(max = 30, message = "소셜 로그인 제공자 값이 너무 깁니다.")
        String provider,

        @JsonAlias("code")
        @NotBlank(message = "소셜 로그인 인가 코드는 필수입니다.")
        @Size(max = 2048, message = "소셜 로그인 인가 코드가 너무 깁니다.")
        String authorizationCode,

        @Size(max = 2048, message = "OAuth redirect URI가 너무 깁니다.")
        String redirectUri,

        @NotBlank(message = "OAuth state는 필수입니다.")
        @Size(max = 128, message = "OAuth state가 너무 깁니다.")
        String state,

        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname,

        @NotEmpty(message = "약관 동의 목록은 필수입니다.")
        @Size(max = 20, message = "약관 동의 항목이 너무 많습니다.")
        List<@Valid Agreement> agreements
) {

    public record Agreement(
            @NotNull(message = "약관 ID는 필수입니다.")
            @Positive(message = "약관 ID는 양수여야 합니다.")
            Long termId,

            @NotNull(message = "약관 동의 여부는 필수입니다.")
            Boolean agreed
    ) {
    }
}
