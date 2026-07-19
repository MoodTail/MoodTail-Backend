package com.example.moodtail.global.auth.model;

public record SocialUserProfile(
        SocialProvider provider,
        String providerUserId,
        String email,
        String nickname
) {
}
