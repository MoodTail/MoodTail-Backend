package com.example.moodtail.domain.user.dto.response;

import com.example.moodtail.domain.user.service.OAuthStateService;

public record OAuthStateResponse(
        String state,
        String codeChallenge,
        String codeChallengeMethod,
        long expiresInSeconds
) {

    public static OAuthStateResponse from(OAuthStateService.OAuthState state) {
        return new OAuthStateResponse(
                state.value(),
                state.codeChallenge(),
                state.codeChallengeMethod(),
                state.expiresInSeconds()
        );
    }
}
