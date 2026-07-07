package com.example.moodtail.domain.user.model;

public record SocialUserProfile(
        String providerUserId,
        String email,
        String nickname
) {
}
