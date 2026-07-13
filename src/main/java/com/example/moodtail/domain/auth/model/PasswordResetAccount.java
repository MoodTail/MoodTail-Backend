package com.example.moodtail.domain.auth.model;

public record PasswordResetAccount(
        Long localAccountId,
        int passwordVersion,
        String email
) {
}
