package com.example.moodtail.domain.auth.model;

public record OAuthState(
        String value,
        String codeChallenge,
        String codeChallengeMethod,
        long expiresInSeconds
) {
}
