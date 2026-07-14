package com.example.moodtail.domain.auth.model;

public record ConsumedOAuthState(Long guestUserId, String codeVerifier) {
}
