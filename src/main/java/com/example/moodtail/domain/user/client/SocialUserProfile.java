package com.example.moodtail.domain.user.client;

import com.example.moodtail.domain.user.enums.SocialProvider;

public record SocialUserProfile(
        SocialProvider provider,
        String providerUserId,
        String email,
        String nickname
) {
}
