package com.example.moodtail.domain.auth.model;

public record SocialSignupTicket(
        String value,
        long expiresInSeconds
) {
}
