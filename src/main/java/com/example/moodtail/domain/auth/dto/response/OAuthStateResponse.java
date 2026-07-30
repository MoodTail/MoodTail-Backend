package com.example.moodtail.domain.auth.dto.response;

import com.example.moodtail.domain.auth.model.OAuthState;
import io.swagger.v3.oas.annotations.media.Schema;

public record OAuthStateResponse(
        @Schema(
                description = "OAuth 인가 요청과 인증 API에 함께 전달할 43자 일회성 state",
                example = "CFrzH3qHdj5bK5H7S9D0B_H9yYcDJSJt2bf6B8b6T4A"
        )
        String state,

        @Schema(
                description = "OAuth 인가 요청에 전달할 PKCE code challenge",
                example = "bKE9UspwyIPg8LsQHkJaiehiTeUdstI5JZOvaoQRgJA"
        )
        String codeChallenge,

        @Schema(description = "PKCE code challenge 생성 방식", allowableValues = "S256", example = "S256")
        String codeChallengeMethod,

        @Schema(description = "state와 PKCE 정보의 남은 유효 시간(초)", example = "300")
        long expiresInSeconds
) {

    public static OAuthStateResponse from(OAuthState state) {
        return new OAuthStateResponse(
                state.value(),
                state.codeChallenge(),
                state.codeChallengeMethod(),
                state.expiresInSeconds()
        );
    }
}
