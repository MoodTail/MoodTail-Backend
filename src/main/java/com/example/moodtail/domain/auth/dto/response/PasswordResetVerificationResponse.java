package com.example.moodtail.domain.auth.dto.response;

public record PasswordResetVerificationResponse(
        String resetToken,
        long expiresInSeconds
) {
}
