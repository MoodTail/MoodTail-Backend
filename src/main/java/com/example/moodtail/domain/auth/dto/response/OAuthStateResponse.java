package com.example.moodtail.domain.auth.dto.response;

import com.example.moodtail.domain.auth.model.OAuthState;

public record OAuthStateResponse(
        String state,
        String codeChallenge,
        String codeChallengeMethod,
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
