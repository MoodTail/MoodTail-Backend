package com.example.moodtail.domain.auth.dto.response;

public record OAuthStateResponse(
        String state,
        String codeChallenge,
        String codeChallengeMethod,
        long expiresInSeconds
) {
}
